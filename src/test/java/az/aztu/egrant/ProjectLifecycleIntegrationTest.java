package az.aztu.egrant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import az.aztu.egrant.iam.domain.AccountStatus;
import az.aztu.egrant.iam.domain.Credential;
import az.aztu.egrant.iam.domain.GlobalRole;
import az.aztu.egrant.iam.domain.User;
import az.aztu.egrant.iam.internal.CredentialRepository;
import az.aztu.egrant.iam.internal.UserRepository;
import az.aztu.egrant.project.internal.ProjectMemberRepository;
import az.aztu.egrant.shared.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end critical path across most modules: lookups → project → team → budget (cap) →
 * submission (lock gate) → expert review → report → admin approval → public views. Exercises the
 * native enums, generated columns, the v_budget_totals view and the submission-guard SPI on real Postgres.
 */
class ProjectLifecycleIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CredentialRepository credentialRepository;
    @Autowired ProjectMemberRepository projectMemberRepository;
    @Autowired JwtService jwtService;

    @Test
    void fullGrantLifecycle() throws Exception {
        String admin = user("9000001", GlobalRole.ADMIN, true);
        String owner = user("9000002", GlobalRole.APPLICANT, true);
        String collab = user("9000003", GlobalRole.APPLICANT, true);
        long collabId = userId("9000003");

        // --- lookups (admin writes) ---
        long institutionId = id(created("/api/v1/institutions", admin,
                Map.of("code", "AZTU", "name", "Azerbaijan Technical University")));
        long priorityId = id(created("/api/v1/priorities", admin,
                Map.of("code", 1, "name", "Information technologies")));

        // --- create project (owner) ---
        MvcResult projectResult = mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("projectName", "Smart Grid",
                                "institutionId", institutionId, "priorityId", priorityId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();
        long projectId = node(projectResult).get("id").asLong();
        long projectCode = node(projectResult).get("projectCode").asLong();

        // --- team: collaborator joins, owner approves ---
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, collab))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/members/" + collabId + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // --- budget: a line item over the cap blocks submission ---
        long itemId = id(created("/api/v1/projects/" + projectId + "/budget/line-items", owner,
                Map.of("category", "EQUIPMENT", "itemName", "Cluster", "unitPrice", 40000, "quantity", 1)));
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/submit")
                        .header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(status().isConflict()); // grand_total 40000 > 30000

        // bring it under the cap and add a salary tied to the approved collaborator
        mockMvc.perform(patch("/api/v1/projects/" + projectId + "/budget/line-items/" + itemId)
                        .header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("unitPrice", 20000))))
                .andExpect(status().isOk());
        long memberId = projectMemberRepository.findByProjectIdAndUserId(projectId, collabId).orElseThrow().getId();
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/budget/salaries")
                        .header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("memberId", memberId, "salaryPerMonth", 1000, "months", 5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(5000)); // generated column

        // totals come from v_budget_totals
        mockMvc.perform(get("/api/v1/projects/" + projectId + "/budget")
                        .header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSalary").value(5000))
                .andExpect(jsonPath("$.totalEquipment").value(20000))
                .andExpect(jsonPath("$.grandTotal").value(25000));

        // --- system lock blocks submission ---
        setLock(admin, true);
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/submit")
                        .header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(status().isConflict());
        setLock(admin, false);

        // --- submit succeeds ---
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/submit")
                        .header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        // --- expert review: assign (only after submit) advances to UNDER_REVIEW ---
        long expertId = id(created("/api/v1/experts", admin, Map.of(
                "email", "expert@example.com", "name", "Eldar", "surname", "Aliyev",
                "personalIdSerialNumber", "AA1234567")));
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/expert-assignments")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("expertId", expertId))))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/projects/" + projectId).header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/assessments")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("expertId", expertId, "score", 85, "note", "Strong"))))
                .andExpect(status().isCreated());

        // --- quarterly report (owner) ---
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/reports")
                        .header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("quarterNumber", 1, "year", 2026,
                                "points", List.of(Map.of("itemNo", 1, "content", "Kicked off"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.points[0].itemNo").value(1));

        // --- admin approval ---
        mockMvc.perform(post("/api/v1/projects/" + projectId + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // --- public views (unauthenticated) ---
        mockMvc.perform(get("/api/v1/public/projects/" + projectCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Smart Grid"))
                .andExpect(jsonPath("$.ownerName").exists());
        mockMvc.perform(get("/api/v1/public/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.projectCode == " + projectCode + ")]").exists());
    }

    @Test
    void joiningRequiresCompletedProfile() throws Exception {
        String admin = user("9100001", GlobalRole.ADMIN, true);
        String owner = user("9100002", GlobalRole.APPLICANT, true);
        String incomplete = user("9100003", GlobalRole.APPLICANT, false);

        long institutionId = id(created("/api/v1/institutions", admin,
                Map.of("code", "AZTU2", "name", "AZTU Campus 2")));
        long projectId = id(mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("projectName", "Robotics", "institutionId", institutionId))))
                .andExpect(status().isCreated())
                .andReturn());

        mockMvc.perform(post("/api/v1/projects/" + projectId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, incomplete))
                .andExpect(status().isForbidden());
    }

    // ---- helpers -----------------------------------------------------------

    private String user(String fin, GlobalRole role, boolean profileCompleted) {
        User u = new User();
        u.setFinKod(fin);
        u.setName("Name" + fin);
        u.setSurname("Sur" + fin);
        u.setPersonalEmail(fin + "@example.com");
        u.setGlobalRole(role);
        u.setProfileCompleted(profileCompleted);
        User saved = userRepository.save(u);
        Credential c = new Credential();
        c.setUserId(saved.getId());
        c.setPasswordHash("hash");
        c.setStatus(AccountStatus.APPROVED);
        credentialRepository.save(c);
        return "Bearer " + jwtService.createAccessToken(saved.getId(), fin, role.name(), profileCompleted);
    }

    private long userId(String fin) {
        return userRepository.findByFinKod(fin).map(User::getId).orElse(-1L);
    }

    private void setLock(String adminToken, boolean locked) throws Exception {
        mockMvc.perform(put("/api/v1/system/lock")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("locked", locked))))
                .andExpect(status().isOk());
    }

    private MvcResult created(String path, String token, Map<String, ?> body) throws Exception {
        return mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private long id(MvcResult result) throws Exception {
        return node(result).get("id").asLong();
    }

    private String json(Map<String, ?> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private JsonNode node(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
