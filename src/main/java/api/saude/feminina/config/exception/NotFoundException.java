package api.saude.feminina.config.exception;

/** Recurso inexistente. Convertido em 404 pelo GlobalExceptionHandler. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
