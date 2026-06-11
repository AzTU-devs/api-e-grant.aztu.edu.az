package az.aztu.egrant.budget.web;

import az.aztu.egrant.budget.domain.BudgetCategory;
import az.aztu.egrant.budget.internal.BudgetService;
import az.aztu.egrant.budget.web.dto.CreateLineItemRequest;
import az.aztu.egrant.budget.web.dto.LineItemResponse;
import az.aztu.egrant.budget.web.dto.UpdateLineItemRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/budget/line-items")
@Tag(name = "Budget line items", description = "Unified cost lines (equipment/services/rent/other)")
public class BudgetLineItemController {

    private final BudgetService budgetService;

    public BudgetLineItemController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    @Operation(summary = "List cost line items (optionally filtered by category)")
    public List<LineItemResponse> list(@AuthenticationPrincipal AuthenticatedUser principal,
                                       @PathVariable Long projectId,
                                       @RequestParam(required = false) BudgetCategory category) {
        return budgetService.listLineItems(projectId, category, principal);
    }

    @PostMapping
    @Operation(summary = "Add a cost line item (category in the body)")
    public ResponseEntity<LineItemResponse> add(@AuthenticationPrincipal AuthenticatedUser principal,
                                                @PathVariable Long projectId,
                                                @Valid @RequestBody CreateLineItemRequest request) {
        return ResponseEntity.status(201).body(budgetService.addLineItem(projectId, principal, request));
    }

    @PatchMapping("/{itemId}")
    @Operation(summary = "Update a cost line item")
    public LineItemResponse update(@AuthenticationPrincipal AuthenticatedUser principal,
                                   @PathVariable Long projectId, @PathVariable Long itemId,
                                   @Valid @RequestBody UpdateLineItemRequest request) {
        return budgetService.updateLineItem(projectId, itemId, principal, request);
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Delete a cost line item")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal,
                                       @PathVariable Long projectId, @PathVariable Long itemId) {
        budgetService.deleteLineItem(projectId, itemId, principal);
        return ResponseEntity.noContent().build();
    }
}
