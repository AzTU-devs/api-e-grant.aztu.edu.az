package az.aztu.egrant.budget.domain;

/** Discriminator for the unified {@code budget_line_items} table (was 4 separate legacy tables). */
public enum BudgetCategory {
    EQUIPMENT,
    SERVICES,
    RENT,
    OTHER
}
