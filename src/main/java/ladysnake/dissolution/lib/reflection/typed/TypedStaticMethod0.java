package ladysnake.dissolution.lib.reflection.typed;

import ladysnake.dissolution.lib.reflection.UncheckedReflectionException;
import org.apiguardian.api.API;

import java.lang.invoke.MethodHandle;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL, since = "3.0.0")
public class TypedStaticMethod0<R> extends TypedStaticMethod {
    public TypedStaticMethod0(MethodHandle methodHandle, String name, Class<?> declaringClass) {
        super(methodHandle, name, declaringClass);
    }

    @SuppressWarnings("unchecked")
    public R invoke() {
        try {
            return (R) methodHandle.invokeExact();
        } catch (Throwable throwable) {
            throw new UncheckedReflectionException(String.format("Could not invoke %s [%s] on %s", name, methodHandle, declaringClass), throwable);
        }
    }
}