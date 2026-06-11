package az.aztu.egrant.budget.web;

import az.aztu.egrant.budget.internal.BudgetService;
import az.aztu.egrant.budget.web.dto.BudgetResponse;
import az.aztu.egrant.budget.web.dto.UpdateBudgetRequest;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/budget")
@Tag(name = "Budget", description = "Budget (smeta) header and computed totals")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    @Operation(summary = "Get the budget header and authoritative computed totals")
    public BudgetResponse get(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long projectId) {
        return budgetService.getBudget(projectId, principal);
    }

    @PutMapping
    @Operation(summary = "Set the policy values (total_fee, defense_fund). Totals are never client-supplied.")
    public BudgetResponse update(@AuthenticationPrincipal AuthenticatedUser principal,
                                 @PathVariable Long projectId, @Valid @RequestBody UpdateBudgetRequest request) {
        return budgetService.updateBudget(projectId, principal, request);
    }
}
