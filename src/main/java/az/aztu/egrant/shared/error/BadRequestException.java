package az.aztu.egrant.shared.error;

import org.springframework.http.HttpStatus;

/** Maps to HTTP 400. */
public class BadRequestException extends DomainException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
