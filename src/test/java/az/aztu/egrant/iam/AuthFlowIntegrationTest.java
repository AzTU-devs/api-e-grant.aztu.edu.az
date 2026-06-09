package az.aztu.egrant.iam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import az.aztu.egrant.AbstractIntegrationTest;
import az.aztu.egrant.iam.domain.AccountStatus;
import az.aztu.egrant.iam.domain.Credential;
import az.aztu.egrant.iam.domain.User;
import az.aztu.egrant.iam.internal.CredentialRepository;
import az.aztu.egrant.iam.internal.OtpCodeRepository;
import az.aztu.egrant.iam.internal.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CredentialRepository credentialRepository;
    @Autowired
    OtpCodeRepository otpCodeRepository;

    @Test
    void registerLoginOtpResetFlow() throws Exception {
        String fin = "1234567";

        // 1. register -> 201
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "finKod", fin,
                                "password", "password1",
                                "academicType", "TEACHER",
                                "name", "Ali",
                                "surname", "Aliyev",
                                "personalEmail", "ali@example.com"))))
                .andExpect(status().isCreated());

        User user = userRepository.findByFinKod(fin).orElseThrow();

        // 2. login while PENDING -> 403
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("finKod", fin, "password", "password1"))))
                .andExpect(status().isForbidden());

        // 3. approve the account
        Credential credential = credentialRepository.findByUserId(user.getId()).orElseThrow();
        credential.setStatus(AccountStatus.APPROVED);
        credentialRepository.save(credential);

        // 4. login -> 200 with token
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("finKod", fin, "password", "password1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("APPLICANT"));

        // 5. request OTP -> 202
        mockMvc.perform(post("/api/v1/auth/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("finKod", fin))))
                .andExpect(status().isAccepted());

        String code = otpCodeRepository
                .findFirstByUserIdAndConsumedAtIsNullOrderByIssuedAtDesc(user.getId())
                .orElseThrow().getCode();

        // 6. verify OTP -> 200 with reset token
        MvcResult verify = mockMvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("finKod", fin, "code", code))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otpToken").isNotEmpty())
                .andReturn();
        String otpToken = node(verify).get("otpToken").asText();

        // 7. reset password -> 204
        mockMvc.perform(post("/api/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", otpToken, "newPassword", "newpass12"))))
                .andExpect(status().isNoContent());

        // 8. login with the new password -> 200
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("finKod", fin, "password", "newpass12"))))
                .andExpect(status().isOk());
    }

    @Test
    void profileCompletionGate() throws Exception {
        String fin = "7654321";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "finKod", fin,
                                "password", "password1",
                                "academicType", "PHD",
                                "name", "Nigar",
                                "surname", "Mammadova",
                                "personalEmail", "nigar@example.com"))))
                .andExpect(status().isCreated());

        User user = userRepository.findByFinKod(fin).orElseThrow();
        Credential credential = credentialRepository.findByUserId(user.getId()).orElseThrow();
        credential.setStatus(AccountStatus.APPROVED);
        credentialRepository.save(credential);

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("finKod", fin, "password", "password1"))))
                .andExpect(status().isOk())
                .andReturn();
        String token = node(login).get("token").asText();

        // partial update -> still incomplete
        mockMvc.perform(put("/api/v1/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("livingLocation", "Baku"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileCompleted").value(false));

        // full required set -> complete
        mockMvc.perform(put("/api/v1/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.ofEntries(
                                Map.entry("fatherName", "Eldar"),
                                Map.entry("bornDate", "1990-01-01"),
                                Map.entry("bornPlace", "Baku"),
                                Map.entry("sex", "F"),
                                Map.entry("citizenship", "Azerbaijani"),
                                Map.entry("personalIdNumber", "AZE12345"),
                                Map.entry("personalMobileNumber", "+994500000000"),
                                Map.entry("workPlace", "AZTU"),
                                Map.entry("department", "CE"),
                                Map.entry("duty", "Researcher")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileCompleted").value(true));

        // /me requires a token
        mockMvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
    }

    private String json(Map<String, ?> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private JsonNode node(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
