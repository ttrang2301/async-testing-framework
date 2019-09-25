package ttrang2301.asynctesting.exception;

public class IgnoredEventException extends RuntimeException {

    public IgnoredEventException() {
    }

    public IgnoredEventException(String message) {
        super(message);
    }

    public IgnoredEventException(String message, Throwable cause) {
        super(message, cause);
    }

    public IgnoredEventException(Throwable cause) {
        super(cause);
    }

    public IgnoredEventException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
