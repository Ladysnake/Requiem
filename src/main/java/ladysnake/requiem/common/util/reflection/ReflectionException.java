package ladysnake.requiem.common.util.reflection;

public class ReflectionException extends Exception {
    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionException(Throwable cause) {
        super(cause);
    }
}
