package az.aztu.egrant.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Base for domain/business exceptions. The carried {@link HttpStatus} is translated to an
 * RFC 7807 {@code ProblemDetail} by {@code GlobalExceptionHandler}.
 */
public abstract class DomainException extends RuntimeException {

    private final HttpStatus status;

    protected DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
