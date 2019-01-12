package ladysnake.reflectivefabric.reflection.typed;

import ladysnake.reflectivefabric.reflection.UncheckedReflectionException;
import org.apiguardian.api.API;

import java.lang.invoke.MethodHandle;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL, since = "2.6.2")
public class TypedMethod2<T, P1, P2, R> extends TypedMethod {
    public TypedMethod2(MethodHandle methodHandle, String name) {
        super(methodHandle, name);
    }

    @SuppressWarnings("unchecked")
    public R invoke(T thisRef, P1 arg1, P2 arg2) {
        try {
            return (R) methodHandle.invoke(thisRef, arg1, arg2);
        } catch (Throwable throwable) {
            throw new UncheckedReflectionException(String.format("Could not invoke %s [%s] on %s with args %s, and %s", name, methodHandle, thisRef, arg1, arg2), throwable);
        }
    }
}
