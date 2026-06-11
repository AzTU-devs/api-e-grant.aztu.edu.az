package az.aztu.egrant.document.internal;

import az.aztu.egrant.budget.api.BudgetTotals;
import az.aztu.egrant.budget.api.CostLine;
import az.aztu.egrant.document.internal.ExportModel.MemberRow;
import az.aztu.egrant.document.internal.ExportModel.SalaryRow;
import az.aztu.egrant.project.api.ActivityView;
import az.aztu.egrant.project.api.ProjectDetail;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Renders a project + smeta to PDF via openhtmltopdf. Builds strict XHTML (so the renderer's XML
 * parser accepts it) and embeds a Unicode font when {@code /fonts/NotoSans-Regular.ttf} is present
 * on the classpath (recommended for Azerbaijani glyphs).
 */
@Component
public class ProjectPdfRenderer {

    private static final Logger log = LoggerFactory.getLogger(ProjectPdfRenderer.class);
    private static final String FONT_PATH = "/fonts/NotoSans-Regular.ttf";
    private static final String FONT_FAMILY = "Noto Sans";

    public byte[] render(ExportModel model) {
        String html = buildXhtml(model);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            registerFontIfPresent(builder);
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new UncheckedIOException("Failed to render project PDF", new java.io.IOException(ex));
        }
    }

    private void registerFontIfPresent(PdfRendererBuilder builder) {
        if (getClass().getResource(FONT_PATH) != null) {
            builder.useFont(() -> getClass().getResourceAsStream(FONT_PATH), FONT_FAMILY);
        } else {
            log.debug("{} not on classpath — using default fonts (non-Latin glyphs may not render).", FONT_PATH);
        }
    }

    private String buildXhtml(ExportModel model) {
        ProjectDetail p = model.project();
        StringBuilder sb = new StringBuilder(4096);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
          .append("<!DOCTYPE html>\n")
          .append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta charset=\"UTF-8\"/>")
          .append("<style>")
          .append("@page { size: A4; margin: 1.5cm; }")
          .append("body { font-family: '").append(FONT_FAMILY).append("', sans-serif; font-size: 11px; color:#222; }")
          .append("h1 { font-size: 18px; color:#0b5394; } h2 { font-size: 14px; color:#0b5394; margin-top:18px; }")
          .append("table { width:100%; border-collapse: collapse; margin-top:6px; }")
          .append("th,td { border:1px solid #999; padding:4px 6px; text-align:left; vertical-align:top; }")
          .append("th { background:#eef3fb; }")
          .append(".label { width:30%; font-weight:bold; background:#f7f9fc; }")
          .append(".num { text-align:right; }")
          .append("</style></head><body>");

        sb.append("<h1>Grant Project ").append(esc(p.projectName())).append("</h1>");

        sb.append("<table>");
        row(sb, "Project code", text(p.projectCode()));
        row(sb, "Status", esc(p.status()));
        row(sb, "Owner", esc(model.ownerName()) + " (FIN " + esc(model.ownerFinKod()) + ")");
        row(sb, "Institution", esc(model.institutionName()));
        row(sb, "Priority", esc(model.priorityName()));
        row(sb, "Deadline", text(p.deadline()));
        sb.append("</table>");

        section(sb, "Purpose", p.projectPurpose());
        section(sb, "Annotation", p.annotation());
        section(sb, "Keywords", p.keyWords());
        section(sb, "Scientific idea", p.scientificIdea());
        section(sb, "Structure", p.structure());
        section(sb, "Team characterization", p.teamCharacterization());
        section(sb, "Monitoring plan", p.monitoringPlan());
        section(sb, "Assessment plan", p.assessmentPlan());
        section(sb, "Requirements", p.requirements());

        sb.append("<h2>Team</h2><table><tr><th>Name</th><th>Role</th><th>Status</th></tr>");
        for (MemberRow m : model.members()) {
            sb.append("<tr><td>").append(esc(m.name())).append("</td><td>").append(esc(m.role()))
              .append("</td><td>").append(esc(m.status())).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<h2>Activity plan</h2><table><tr><th>Month</th><th>Activity</th></tr>");
        for (ActivityView a : model.activities()) {
            sb.append("<tr><td>").append(text(a.month())).append("</td><td>")
              .append(esc(a.activityName())).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<h2>Budget — salaries</h2>")
          .append("<table><tr><th>Member</th><th class=\"num\">Per month</th><th class=\"num\">Months</th>")
          .append("<th class=\"num\">Total</th></tr>");
        for (SalaryRow s : model.salaries()) {
            sb.append("<tr><td>").append(esc(s.memberName())).append("</td><td class=\"num\">")
              .append(s.salaryPerMonth()).append("</td><td class=\"num\">").append(s.months())
              .append("</td><td class=\"num\">").append(s.totalAmount()).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<h2>Budget — cost items</h2>")
          .append("<table><tr><th>Category</th><th>Item</th><th>Unit</th><th class=\"num\">Unit price</th>")
          .append("<th class=\"num\">Qty</th><th class=\"num\">Duration</th><th class=\"num\">Total</th></tr>");
        for (CostLine c : model.lineItems()) {
            sb.append("<tr><td>").append(esc(c.category())).append("</td><td>").append(esc(c.itemName()))
              .append("</td><td>").append(esc(c.unitOfMeasure())).append("</td><td class=\"num\">")
              .append(c.unitPrice()).append("</td><td class=\"num\">").append(c.quantity())
              .append("</td><td class=\"num\">").append(c.duration()).append("</td><td class=\"num\">")
              .append(c.totalAmount()).append("</td></tr>");
        }
        sb.append("</table>");

        BudgetTotals t = model.totals();
        sb.append("<h2>Budget — totals</h2><table>");
        row(sb, "Salaries", num(t == null ? 0 : t.totalSalary()));
        row(sb, "Equipment", num(t == null ? 0 : t.totalEquipment()));
        row(sb, "Services", num(t == null ? 0 : t.totalServices()));
        row(sb, "Rent", num(t == null ? 0 : t.totalRent()));
        row(sb, "Other", num(t == null ? 0 : t.totalOther()));
        row(sb, "Total fee", num(t == null ? 0 : t.totalFee()));
        row(sb, "Defense fund", num(t == null ? 0 : t.defenseFund()));
        row(sb, "Grand total", num(t == null ? 0 : t.grandTotal()));
        sb.append("</table>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private static void section(StringBuilder sb, String title, String body) {
        if (body == null || body.isBlank()) {
            return;
        }
        sb.append("<h2>").append(esc(title)).append("</h2><p>").append(esc(body)).append("</p>");
    }

    private static void row(StringBuilder sb, String label, String value) {
        sb.append("<tr><td class=\"label\">").append(esc(label)).append("</td><td>")
          .append(value).append("</td></tr>");
    }

    private static String num(int value) {
        return "<span class=\"num\">" + value + "</span>";
    }

    private static String text(Object value) {
        return value == null ? "" : esc(value.toString());
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
