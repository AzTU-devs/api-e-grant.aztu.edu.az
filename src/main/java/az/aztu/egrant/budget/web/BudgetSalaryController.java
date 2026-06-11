package az.aztu.egrant.budget.web;

import az.aztu.egrant.budget.internal.BudgetService;
import az.aztu.egrant.budget.web.dto.CreateSalaryRequest;
import az.aztu.egrant.budget.web.dto.SalaryResponse;
import az.aztu.egrant.budget.web.dto.UpdateSalaryRequest;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/budget/salaries")
@Tag(name = "Budget salaries", description = "Salary lines tied to team members")
public class BudgetSalaryController {

    private final BudgetService budgetService;

    public BudgetSalaryController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    @Operation(summary = "List salary lines")
    public List<SalaryResponse> list(@AuthenticationPrincipal AuthenticatedUser principal,
                                     @PathVariable Long projectId) {
        return budgetService.listSalaries(projectId, principal);
    }

    @PostMapping
    @Operation(summary = "Add a salary line for an approved team member")
    public ResponseEntity<SalaryResponse> add(@AuthenticationPrincipal AuthenticatedUser principal,
                                              @PathVariable Long projectId,
                                              @Valid @RequestBody CreateSalaryRequest request) {
        return ResponseEntity.status(201).body(budgetService.addSalary(projectId, principal, request));
    }

    @PatchMapping("/{salaryId}")
    @Operation(summary = "Update a salary line")
    public SalaryResponse update(@AuthenticationPrincipal AuthenticatedUser principal,
                                 @PathVariable Long projectId, @PathVariable Long salaryId,
                                 @Valid @RequestBody UpdateSalaryRequest request) {
        return budgetService.updateSalary(projectId, salaryId, principal, request);
    }

    @DeleteMapping("/{salaryId}")
    @Operation(summary = "Delete a salary line")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal,
                                       @PathVariable Long projectId, @PathVariable Long salaryId) {
        budgetService.deleteSalary(projectId, salaryId, principal);
        return ResponseEntity.noContent().build();
    }
}
