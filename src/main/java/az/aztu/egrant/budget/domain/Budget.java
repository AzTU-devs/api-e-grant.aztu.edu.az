package az.aztu.egrant.budget.domain;

import az.aztu.egrant.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Budget header, 1:1 with a project. {@code total_fee}/{@code defense_fund} are policy values; the
 * other totals are cached rollups refreshed transactionally from the line items, with
 * {@code v_budget_totals} as the authority.
 */
@Entity
@Table(name = "budgets")
@Getter
@Setter
public class Budget extends BaseEntity {

    @Column(name = "project_id", nullable = false, unique = true)
    private Long projectId;

    @Column(name = "total_fee", nullable = false)
    private int totalFee = 0;

    @Column(name = "defense_fund", nullable = false)
    private int defenseFund = 0;

    @Column(name = "total_salary", nullable = false)
    private int totalSalary = 0;

    @Column(name = "total_equipment", nullable = false)
    private int totalEquipment = 0;

    @Column(name = "total_services", nullable = false)
    private int totalServices = 0;

    @Column(name = "total_rent", nullable = false)
    private int totalRent = 0;

    @Column(name = "total_other", nullable = false)
    private int totalOther = 0;

    @Column(name = "grand_total", nullable = false)
    private int grandTotal = 0;
}
