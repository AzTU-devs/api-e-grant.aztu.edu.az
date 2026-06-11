/**
 * Quarterly reporting: a report per {@code (project, year, quarter)} whose 17 points are stored
 * as normalized {@code quarterly_report_items} rows (replaces the legacy {@code point_1..17}
 * repeating columns). Depends on {@code project} for ownership checks.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Report")
package az.aztu.egrant.report;
