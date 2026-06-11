package az.aztu.egrant.document.internal;

import az.aztu.egrant.budget.api.BudgetTotals;
import az.aztu.egrant.budget.api.CostLine;
import az.aztu.egrant.document.internal.ExportModel.MemberRow;
import az.aztu.egrant.document.internal.ExportModel.SalaryRow;
import az.aztu.egrant.project.api.ActivityView;
import az.aztu.egrant.project.api.ProjectDetail;
import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/** Renders a project + smeta to an .xlsx workbook (Apache POI). */
@Component
public class ProjectExcelRenderer {

    public byte[] render(ExportModel model) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle header = headerStyle(wb);
            writeProjectSheet(wb, header, model);
            writeTeamSheet(wb, header, model);
            writeActivitiesSheet(wb, header, model);
            writeBudgetSheet(wb, header, model);
            wb.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new UncheckedIOException("Failed to render project Excel", new java.io.IOException(ex));
        }
    }

    private void writeProjectSheet(Workbook wb, CellStyle header, ExportModel model) {
        ProjectDetail p = model.project();
        Sheet sheet = wb.createSheet("Project");
        int r = 0;
        r = labelled(sheet, r, header, "Project code", String.valueOf(p.projectCode()));
        r = labelled(sheet, r, header, "Project name", p.projectName());
        r = labelled(sheet, r, header, "Status", p.status());
        r = labelled(sheet, r, header, "Owner", model.ownerName());
        r = labelled(sheet, r, header, "Owner FIN", model.ownerFinKod());
        r = labelled(sheet, r, header, "Institution", model.institutionName());
        r = labelled(sheet, r, header, "Priority", model.priorityName());
        r = labelled(sheet, r, header, "Deadline", p.deadline() == null ? "" : p.deadline().toString());
        r = labelled(sheet, r, header, "Purpose", p.projectPurpose());
        r = labelled(sheet, r, header, "Annotation", p.annotation());
        r = labelled(sheet, r, header, "Keywords", p.keyWords());
        r = labelled(sheet, r, header, "Scientific idea", p.scientificIdea());
        r = labelled(sheet, r, header, "Structure", p.structure());
        r = labelled(sheet, r, header, "Team characterization", p.teamCharacterization());
        r = labelled(sheet, r, header, "Monitoring plan", p.monitoringPlan());
        r = labelled(sheet, r, header, "Assessment plan", p.assessmentPlan());
        labelled(sheet, r, header, "Requirements", p.requirements());
        autoSize(sheet, 2);
    }

    private void writeTeamSheet(Workbook wb, CellStyle header, ExportModel model) {
        Sheet sheet = wb.createSheet("Team");
        headerRow(sheet, header, "Name", "Role", "Status");
        int r = 1;
        for (MemberRow m : model.members()) {
            Row row = sheet.createRow(r++);
            set(row, 0, m.name());
            set(row, 1, m.role());
            set(row, 2, m.status());
        }
        autoSize(sheet, 3);
    }

    private void writeActivitiesSheet(Workbook wb, CellStyle header, ExportModel model) {
        Sheet sheet = wb.createSheet("Activities");
        headerRow(sheet, header, "Month", "Activity");
        int r = 1;
        for (ActivityView a : model.activities()) {
            Row row = sheet.createRow(r++);
            setNumber(row, 0, a.month());
            set(row, 1, a.activityName());
        }
        autoSize(sheet, 2);
    }

    private void writeBudgetSheet(Workbook wb, CellStyle header, ExportModel model) {
        Sheet sheet = wb.createSheet("Budget");
        int r = 0;

        title(sheet, r++, header, "Salaries");
        headerRow(sheet, r++, header, "Member", "Per month", "Months", "Total");
        for (SalaryRow s : model.salaries()) {
            Row row = sheet.createRow(r++);
            set(row, 0, s.memberName());
            setNumber(row, 1, s.salaryPerMonth());
            setNumber(row, 2, s.months());
            setNumber(row, 3, s.totalAmount());
        }

        r++;
        title(sheet, r++, header, "Cost items");
        headerRow(sheet, r++, header, "Category", "Item", "Unit", "Unit price", "Qty", "Duration", "Total");
        for (CostLine c : model.lineItems()) {
            Row row = sheet.createRow(r++);
            set(row, 0, c.category());
            set(row, 1, c.itemName());
            set(row, 2, c.unitOfMeasure());
            setNumber(row, 3, c.unitPrice());
            setNumber(row, 4, c.quantity());
            setNumber(row, 5, c.duration());
            setNumber(row, 6, c.totalAmount());
        }

        r++;
        title(sheet, r++, header, "Totals");
        BudgetTotals t = model.totals();
        r = totalRow(sheet, r, "Salaries", t == null ? 0 : t.totalSalary());
        r = totalRow(sheet, r, "Equipment", t == null ? 0 : t.totalEquipment());
        r = totalRow(sheet, r, "Services", t == null ? 0 : t.totalServices());
        r = totalRow(sheet, r, "Rent", t == null ? 0 : t.totalRent());
        r = totalRow(sheet, r, "Other", t == null ? 0 : t.totalOther());
        r = totalRow(sheet, r, "Total fee", t == null ? 0 : t.totalFee());
        r = totalRow(sheet, r, "Defense fund", t == null ? 0 : t.defenseFund());
        totalRow(sheet, r, "Grand total", t == null ? 0 : t.grandTotal());
        autoSize(sheet, 7);
    }

    // ---- helpers -----------------------------------------------------------

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void headerRow(Sheet sheet, CellStyle header, String... titles) {
        headerRow(sheet, 0, header, titles);
    }

    private void headerRow(Sheet sheet, int rowIdx, CellStyle header, String... titles) {
        Row row = sheet.createRow(rowIdx);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(header);
        }
    }

    private void title(Sheet sheet, int rowIdx, CellStyle header, String text) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(header);
    }

    private int labelled(Sheet sheet, int rowIdx, CellStyle header, String label, String value) {
        Row row = sheet.createRow(rowIdx);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(header);
        set(row, 1, value);
        return rowIdx + 1;
    }

    private int totalRow(Sheet sheet, int rowIdx, String label, int value) {
        Row row = sheet.createRow(rowIdx);
        set(row, 0, label);
        setNumber(row, 1, value);
        return rowIdx + 1;
    }

    private void set(Row row, int col, String value) {
        row.createCell(col).setCellValue(value == null ? "" : value);
    }

    private void setNumber(Row row, int col, Integer value) {
        if (value == null) {
            row.createCell(col).setCellValue("");
        } else {
            row.createCell(col).setCellValue(value.doubleValue());
        }
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
