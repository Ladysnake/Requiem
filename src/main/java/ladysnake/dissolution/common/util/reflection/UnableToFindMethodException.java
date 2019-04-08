package ladysnake.dissolution.common.util.reflection;

public class UnableToFindMethodException extends UncheckedReflectionException {
    public UnableToFindMethodException(NoSuchMethodException cause) {
        super(cause);
    }

    public UnableToFindMethodException(ReflectiveOperationException cause) {
        super(cause);
    }
}
