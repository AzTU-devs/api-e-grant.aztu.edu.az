/**
 * Unauthenticated public views under {@code /api/v1/public}: approved-project listing/detail and
 * the priorities tree (legacy "leads-tree"). Read-only aggregator over other modules' directories;
 * exposes only sanitized data (no contact, personal, budget or review information).
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Public API")
package az.aztu.egrant.publicapi;
