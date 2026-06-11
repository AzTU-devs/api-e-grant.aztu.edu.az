package az.aztu.egrant.budget.internal;

import az.aztu.egrant.budget.api.CostLine;
import az.aztu.egrant.budget.api.SalaryLine;
import az.aztu.egrant.budget.domain.BudgetLineItem;
import az.aztu.egrant.budget.domain.BudgetSalary;
import az.aztu.egrant.budget.web.dto.LineItemResponse;
import az.aztu.egrant.budget.web.dto.SalaryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    SalaryResponse toSalaryResponse(BudgetSalary salary);

    LineItemResponse toLineItemResponse(BudgetLineItem item);

    SalaryLine toSalaryLine(BudgetSalary salary);

    CostLine toCostLine(BudgetLineItem item);
}
