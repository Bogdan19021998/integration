package ai.distil.integration.exception;

public class ConverterException extends RuntimeException {
    private Boolean serialization;

    public ConverterException(Boolean serialization, String message, Throwable cause) {
        super(message, cause);
        this.serialization = serialization;
    }
}
