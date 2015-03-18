package neon.util.reflection;

public class ReflectionException extends RuntimeException {

    public ReflectionException(Throwable cause) {
        super(cause);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
