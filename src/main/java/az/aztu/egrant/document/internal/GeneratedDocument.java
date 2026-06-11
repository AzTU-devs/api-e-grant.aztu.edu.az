package az.aztu.egrant.document.internal;

/** A rendered document ready to stream back to the client. */
public record GeneratedDocument(String filename, String contentType, byte[] content) {
}
