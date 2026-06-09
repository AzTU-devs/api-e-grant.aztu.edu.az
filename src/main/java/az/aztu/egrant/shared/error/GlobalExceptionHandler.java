package az.aztu.egrant.shared.error;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Translates exceptions into RFC 7807 {@link ProblemDetail} responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI BASE = URI.create("https://e-grant.aztu.edu.az/problems/");

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return problem(ex.getStatus(), ex.getMessage(), ex.getClass().getSimpleName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed", "validation-error");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraint(ConstraintViolationException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage(), "validation-error");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "Data integrity violation", "data-integrity");
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ProblemDetail handleAuth(Exception ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentication failed", "unauthorized");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Access denied", "forbidden");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", "internal-error");
    }

    private ProblemDetail problem(HttpStatus status, String detail, String type) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(BASE.resolve(type));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
