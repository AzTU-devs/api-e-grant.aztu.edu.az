/**
 * Budget (smeta): the 1:1 budget header per project, salary lines and unified cost line items
 * (equipment/services/rent/other discriminated by {@code budget_category}). Line totals are
 * DB-computed ({@code GENERATED … STORED}); the authoritative rollups come from the
 * {@code v_budget_totals} view. Contributes a {@link az.aztu.egrant.project.api.ProjectSubmissionGuard}
 * enforcing {@code grand_total ≤ max_budget_amount}. Depends on {@code project} (never the reverse).
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Budget")
package az.aztu.egrant.budget;
