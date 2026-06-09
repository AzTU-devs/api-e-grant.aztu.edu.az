package az.aztu.egrant.shared.error;

import org.springframework.http.HttpStatus;

/** Maps to HTTP 409 (e.g. collaborator-limit and budget-cap violations). */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
