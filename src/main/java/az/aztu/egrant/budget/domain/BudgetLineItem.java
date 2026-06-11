package az.aztu.egrant.budget.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

/**
 * A unified cost line item (equipment/services/rent/other via {@code category}). {@code total_amount}
 * is DB-computed ({@code unit_price * quantity * duration}). Replaces 4 near-identical legacy tables.
 */
@Entity
@Table(name = "budget_line_items")
@Getter
@Setter
public class BudgetLineItem extends BaseEntity {

    @Column(name = "budget_id", nullable = false)
    private Long budgetId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "category", columnDefinition = "budget_category", nullable = false)
    private BudgetCategory category;

    @Column(name = "item_name", nullable = false, columnDefinition = "text")
    private String itemName;

    @Column(name = "unit_of_measure", columnDefinition = "text")
    private String unitOfMeasure;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "duration", nullable = false)
    private Integer duration = 1;

    /** {@code GENERATED ALWAYS AS (unit_price * quantity * duration) STORED} — read back after write. */
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "total_amount", insertable = false, updatable = false)
    private Integer totalAmount;
}
