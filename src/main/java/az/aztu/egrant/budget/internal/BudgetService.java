package az.aztu.egrant.budget.internal;

import az.aztu.egrant.budget.domain.Budget;
import az.aztu.egrant.budget.domain.BudgetCategory;
import az.aztu.egrant.budget.domain.BudgetLineItem;
import az.aztu.egrant.budget.domain.BudgetSalary;
import az.aztu.egrant.budget.domain.BudgetTotalsView;
import az.aztu.egrant.budget.web.dto.BudgetResponse;
import az.aztu.egrant.budget.web.dto.CreateLineItemRequest;
import az.aztu.egrant.budget.web.dto.CreateSalaryRequest;
import az.aztu.egrant.budget.web.dto.LineItemResponse;
import az.aztu.egrant.budget.web.dto.SalaryResponse;
import az.aztu.egrant.budget.web.dto.UpdateBudgetRequest;
import az.aztu.egrant.budget.web.dto.UpdateLineItemRequest;
import az.aztu.egrant.budget.web.dto.UpdateSalaryRequest;
import az.aztu.egrant.project.api.MembershipDirectory;
import az.aztu.egrant.project.api.ProjectDirectory;
import az.aztu.egrant.project.api.ProjectMemberInfo;
import az.aztu.egrant.project.api.ProjectSummary;
import az.aztu.egrant.shared.error.BadRequestException;
import az.aztu.egrant.shared.error.ConflictException;
import az.aztu.egrant.shared.error.ForbiddenException;
import az.aztu.egrant.shared.error.NotFoundException;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Budget management. Line totals are DB-generated; the cached header rollups are refreshed
 * transactionally from {@code v_budget_totals} after every mutation, and reads return the
 * authoritative view figures (never client-supplied totals).
 */
@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetSalaryRepository salaryRepository;
    private final BudgetLineItemRepository lineItemRepository;
    private final BudgetTotalsViewRepository totalsViewRepository;
    private final BudgetMapper mapper;
    private final ProjectDirectory projectDirectory;
    private final MembershipDirectory membershipDirectory;

    public BudgetService(BudgetRepository budgetRepository, BudgetSalaryRepository salaryRepository,
                         BudgetLineItemRepository lineItemRepository,
                         BudgetTotalsViewRepository totalsViewRepository, BudgetMapper mapper,
                         ProjectDirectory projectDirectory, MembershipDirectory membershipDirectory) {
        this.budgetRepository = budgetRepository;
        this.salaryRepository = salaryRepository;
        this.lineItemRepository = lineItemRepository;
        this.totalsViewRepository = totalsViewRepository;
        this.mapper = mapper;
        this.projectDirectory = projectDirectory;
        this.membershipDirectory = membershipDirectory;
    }

    // ---- budget header -----------------------------------------------------

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(Long projectId, AuthenticatedUser actor) {
        requireReadAccess(projectId, actor);
        Budget b = budgetRepository.findByProjectId(projectId).orElse(null);
        if (b == null) {
            return new BudgetResponse(null, projectId, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        return response(b);
    }

    @Transactional
    public BudgetResponse updateBudget(Long projectId, AuthenticatedUser actor, UpdateBudgetRequest req) {
        requireWriteAccess(projectId, actor);
        Budget b = ensureBudget(projectId);
        if (req.totalFee() != null) b.setTotalFee(req.totalFee());
        if (req.defenseFund() != null) b.setDefenseFund(req.defenseFund());
        refreshTotals(b);
        return response(b);
    }

    // ---- salaries ----------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SalaryResponse> listSalaries(Long projectId, AuthenticatedUser actor) {
        requireReadAccess(projectId, actor);
        return budgetRepository.findByProjectId(projectId)
                .map(b -> salaryRepository.findByBudgetId(b.getId()).stream()
                        .map(mapper::toSalaryResponse).toList())
                .orElseGet(List::of);
    }

    @Transactional
    public SalaryResponse addSalary(Long projectId, AuthenticatedUser actor, CreateSalaryRequest req) {
        requireWriteAccess(projectId, actor);
        Budget b = ensureBudget(projectId);
        requireApprovedMemberOfProject(projectId, req.memberId());
        if (salaryRepository.existsByBudgetIdAndMemberId(b.getId(), req.memberId())) {
            throw new ConflictException("A salary line already exists for this team member.");
        }
        BudgetSalary s = new BudgetSalary();
        s.setBudgetId(b.getId());
        s.setMemberId(req.memberId());
        s.setSalaryPerMonth(req.salaryPerMonth());
        s.setMonths(req.months());
        BudgetSalary saved = salaryRepository.saveAndFlush(s);
        refreshTotals(b);
        return mapper.toSalaryResponse(saved);
    }

    @Transactional
    public SalaryResponse updateSalary(Long projectId, Long salaryId, AuthenticatedUser actor,
                                       UpdateSalaryRequest req) {
        Budget b = requireWriteAccessAndBudget(projectId, actor);
        BudgetSalary s = requireSalary(b.getId(), salaryId);
        if (req.salaryPerMonth() != null) s.setSalaryPerMonth(req.salaryPerMonth());
        if (req.months() != null) s.setMonths(req.months());
        BudgetSalary saved = salaryRepository.saveAndFlush(s);
        refreshTotals(b);
        return mapper.toSalaryResponse(saved);
    }

    @Transactional
    public void deleteSalary(Long projectId, Long salaryId, AuthenticatedUser actor) {
        Budget b = requireWriteAccessAndBudget(projectId, actor);
        salaryRepository.delete(requireSalary(b.getId(), salaryId));
        refreshTotals(b);
    }

    // ---- line items --------------------------------------------------------

    @Transactional(readOnly = true)
    public List<LineItemResponse> listLineItems(Long projectId, BudgetCategory category, AuthenticatedUser actor) {
        requireReadAccess(projectId, actor);
        Budget b = budgetRepository.findByProjectId(projectId).orElse(null);
        if (b == null) {
            return List.of();
        }
        List<BudgetLineItem> items = category != null
                ? lineItemRepository.findByBudgetIdAndCategoryOrderByIdAsc(b.getId(), category)
                : lineItemRepository.findByBudgetIdOrderByCategoryAscIdAsc(b.getId());
        return items.stream().map(mapper::toLineItemResponse).toList();
    }

    @Transactional
    public LineItemResponse addLineItem(Long projectId, AuthenticatedUser actor, CreateLineItemRequest req) {
        requireWriteAccess(projectId, actor);
        Budget b = ensureBudget(projectId);
        BudgetLineItem item = new BudgetLineItem();
        item.setBudgetId(b.getId());
        item.setCategory(req.category());
        item.setItemName(req.itemName());
        item.setUnitOfMeasure(req.unitOfMeasure());
        item.setUnitPrice(req.unitPrice());
        item.setQuantity(req.quantity());
        item.setDuration(req.duration() == null ? 1 : req.duration());
        BudgetLineItem saved = lineItemRepository.saveAndFlush(item);
        refreshTotals(b);
        return mapper.toLineItemResponse(saved);
    }

    @Transactional
    public LineItemResponse updateLineItem(Long projectId, Long itemId, AuthenticatedUser actor,
                                           UpdateLineItemRequest req) {
        Budget b = requireWriteAccessAndBudget(projectId, actor);
        BudgetLineItem item = requireLineItem(b.getId(), itemId);
        if (req.category() != null) item.setCategory(req.category());
        if (req.itemName() != null) item.setItemName(req.itemName());
        if (req.unitOfMeasure() != null) item.setUnitOfMeasure(req.unitOfMeasure());
        if (req.unitPrice() != null) item.setUnitPrice(req.unitPrice());
        if (req.quantity() != null) item.setQuantity(req.quantity());
        if (req.duration() != null) item.setDuration(req.duration());
        BudgetLineItem saved = lineItemRepository.saveAndFlush(item);
        refreshTotals(b);
        return mapper.toLineItemResponse(saved);
    }

    @Transactional
    public void deleteLineItem(Long projectId, Long itemId, AuthenticatedUser actor) {
        Budget b = requireWriteAccessAndBudget(projectId, actor);
        lineItemRepository.delete(requireLineItem(b.getId(), itemId));
        refreshTotals(b);
    }

    // ---- helpers -----------------------------------------------------------

    private Budget ensureBudget(Long projectId) {
        return budgetRepository.findByProjectId(projectId).orElseGet(() -> {
            Budget b = new Budget();
            b.setProjectId(projectId);
            return budgetRepository.save(b);
        });
    }

    /** Flush pending child writes + header, then copy authoritative rollups from the view into the cache. */
    private void refreshTotals(Budget b) {
        budgetRepository.saveAndFlush(b);
        totalsViewRepository.findById(b.getId()).ifPresent(v -> {
            b.setTotalSalary(nz(v.getTotalSalary()));
            b.setTotalEquipment(nz(v.getTotalEquipment()));
            b.setTotalServices(nz(v.getTotalServices()));
            b.setTotalRent(nz(v.getTotalRent()));
            b.setTotalOther(nz(v.getTotalOther()));
            b.setGrandTotal(nz(v.getGrandTotal()));
        });
    }

    private BudgetResponse response(Budget b) {
        BudgetTotalsView v = totalsViewRepository.findById(b.getId()).orElse(null);
        return new BudgetResponse(b.getId(), b.getProjectId(), b.getTotalFee(), b.getDefenseFund(),
                v == null ? 0 : nz(v.getTotalSalary()),
                v == null ? 0 : nz(v.getTotalEquipment()),
                v == null ? 0 : nz(v.getTotalServices()),
                v == null ? 0 : nz(v.getTotalRent()),
                v == null ? 0 : nz(v.getTotalOther()),
                v == null ? 0 : nz(v.getGrandTotal()));
    }

    private ProjectSummary requireProject(Long projectId) {
        return projectDirectory.findById(projectId)
                .orElseThrow(() -> NotFoundException.of("Project", projectId));
    }

    private void requireReadAccess(Long projectId, AuthenticatedUser actor) {
        requireProject(projectId);
        if (isAdmin(actor) || membershipDirectory.isApprovedMember(projectId, actor.userId())) {
            return;
        }
        throw new ForbiddenException("Only team members or an admin may view this budget.");
    }

    private void requireWriteAccess(Long projectId, AuthenticatedUser actor) {
        ProjectSummary p = requireProject(projectId);
        if (!p.ownerId().equals(actor.userId()) && !isAdmin(actor)) {
            throw new ForbiddenException("Only the project owner or an admin may edit this budget.");
        }
    }

    private Budget requireWriteAccessAndBudget(Long projectId, AuthenticatedUser actor) {
        requireWriteAccess(projectId, actor);
        return budgetRepository.findByProjectId(projectId)
                .orElseThrow(() -> NotFoundException.of("Budget", projectId));
    }

    private void requireApprovedMemberOfProject(Long projectId, Long memberId) {
        ProjectMemberInfo member = membershipDirectory.findMember(memberId)
                .orElseThrow(() -> new BadRequestException("Unknown team member: " + memberId));
        if (!member.projectId().equals(projectId) || !member.isApproved()) {
            throw new BadRequestException("Member " + memberId + " is not an approved member of this project.");
        }
    }

    private BudgetSalary requireSalary(Long budgetId, Long salaryId) {
        BudgetSalary s = salaryRepository.findById(salaryId)
                .orElseThrow(() -> NotFoundException.of("Salary", salaryId));
        if (!s.getBudgetId().equals(budgetId)) {
            throw new ConflictException("Salary " + salaryId + " does not belong to this project's budget.");
        }
        return s;
    }

    private BudgetLineItem requireLineItem(Long budgetId, Long itemId) {
        BudgetLineItem item = lineItemRepository.findById(itemId)
                .orElseThrow(() -> NotFoundException.of("Line item", itemId));
        if (!item.getBudgetId().equals(budgetId)) {
            throw new ConflictException("Line item " + itemId + " does not belong to this project's budget.");
        }
        return item;
    }

    private boolean isAdmin(AuthenticatedUser actor) {
        return "ADMIN".equals(actor.role()) || "SUPER_ADMIN".equals(actor.role());
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
