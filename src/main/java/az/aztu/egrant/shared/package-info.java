/**
 * Shared kernel: base entity, error handling, JWT/security, CORS, OpenAPI, file storage,
 * mail abstraction and rate limiting. Declared {@code OPEN} so every other module may use
 * all of its types (including those in sub-packages) without boundary violations.
 */
@org.springframework.modulith.ApplicationModule(
        type = org.springframework.modulith.ApplicationModule.Type.OPEN,
        displayName = "Shared Kernel")
package az.aztu.egrant.shared;
