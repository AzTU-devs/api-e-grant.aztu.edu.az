package az.aztu.egrant.shared.error;

import org.springframework.http.HttpStatus;

/** Maps to HTTP 403 (e.g. action requires a completed profile, or insufficient rights). */
public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
