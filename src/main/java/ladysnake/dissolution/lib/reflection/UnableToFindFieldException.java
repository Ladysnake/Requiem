package ladysnake.dissolution.lib.reflection;

public class UnableToFindFieldException extends UncheckedReflectionException {
    public UnableToFindFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnableToFindFieldException(Throwable cause) {
        super(cause);
    }
}
