package ladysnake.dissolution.lib.reflection.typed;

import ladysnake.dissolution.lib.reflection.UncheckedReflectionException;
import org.apiguardian.api.API;

import java.lang.invoke.MethodHandle;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL, since = "2.6.2")
public class TypedMethod1<T, P1, R> extends TypedMethod {
    public TypedMethod1(MethodHandle methodHandle, String name) {
        super(methodHandle, name);
    }

    @SuppressWarnings("unchecked")
    public R invoke(T thisRef, P1 arg) {
        try {
            return (R) methodHandle.invoke(thisRef, arg);
        } catch (Throwable throwable) {
            throw new UncheckedReflectionException(String.format("Could not invoke %s [%s] on %s with arg %s", name, methodHandle, thisRef, arg), throwable);
        }
    }
}
