package az.aztu.egrant.budget.internal;

import az.aztu.egrant.budget.api.BudgetDirectory;
import az.aztu.egrant.budget.api.BudgetTotals;
import az.aztu.egrant.budget.api.CostLine;
import az.aztu.egrant.budget.api.SalaryLine;
import az.aztu.egrant.budget.domain.BudgetTotalsView;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link BudgetDirectory} implementation exposed to {@code document}; totals come from the view. */
@Service
@Transactional(readOnly = true)
public class BudgetDirectoryImpl implements BudgetDirectory {

    private final BudgetRepository budgetRepository;
    private final BudgetSalaryRepository salaryRepository;
    private final BudgetLineItemRepository lineItemRepository;
    private final BudgetTotalsViewRepository totalsViewRepository;
    private final BudgetMapper mapper;

    public BudgetDirectoryImpl(BudgetRepository budgetRepository, BudgetSalaryRepository salaryRepository,
                               BudgetLineItemRepository lineItemRepository,
                               BudgetTotalsViewRepository totalsViewRepository, BudgetMapper mapper) {
        this.budgetRepository = budgetRepository;
        this.salaryRepository = salaryRepository;
        this.lineItemRepository = lineItemRepository;
        this.totalsViewRepository = totalsViewRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<BudgetTotals> totalsForProject(Long projectId) {
        return budgetRepository.findByProjectId(projectId).map(b -> {
            BudgetTotalsView v = totalsViewRepository.findById(b.getId()).orElse(null);
            return new BudgetTotals(b.getTotalFee(), b.getDefenseFund(),
                    nz(v == null ? null : v.getTotalSalary()),
                    nz(v == null ? null : v.getTotalEquipment()),
                    nz(v == null ? null : v.getTotalServices()),
                    nz(v == null ? null : v.getTotalRent()),
                    nz(v == null ? null : v.getTotalOther()),
                    nz(v == null ? null : v.getGrandTotal()));
        });
    }

    @Override
    public List<SalaryLine> salariesForProject(Long projectId) {
        return budgetRepository.findByProjectId(projectId)
                .map(b -> salaryRepository.findByBudgetId(b.getId()).stream()
                        .map(mapper::toSalaryLine).toList())
                .orElseGet(List::of);
    }

    @Override
    public List<CostLine> lineItemsForProject(Long projectId) {
        return budgetRepository.findByProjectId(projectId)
                .map(b -> lineItemRepository.findByBudgetIdOrderByCategoryAscIdAsc(b.getId()).stream()
                        .map(mapper::toCostLine).toList())
                .orElseGet(List::of);
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
