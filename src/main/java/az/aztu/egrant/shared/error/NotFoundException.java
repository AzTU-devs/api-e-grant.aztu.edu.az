package az.aztu.egrant.shared.error;

import org.springframework.http.HttpStatus;

/** Maps to HTTP 404. */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " not found: " + id);
    }
}
