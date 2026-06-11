/**
 * Institution lookup (was the legacy {@code institution} table): the organisations users and
 * projects are scoped to. Exposes a cross-module {@code InstitutionDirectory} so {@code project}
 * and {@code iam} can validate/resolve an institution by id without reaching into this module.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Institution")
package az.aztu.egrant.institution;
