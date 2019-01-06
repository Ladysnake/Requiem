package ladysnake.dissolution.lib.reflection.typed;

import ladysnake.dissolution.lib.reflection.UncheckedReflectionException;
import org.apiguardian.api.API;

import java.lang.invoke.MethodHandle;

@API(status = API.Status.EXPERIMENTAL, since = "2.6.2")
public class TypedMethod5<T, P1, P2, P3, P4, P5, R> extends TypedMethod {
    public TypedMethod5(MethodHandle methodHandle, String name) {
        super(methodHandle, name);
    }

    @SuppressWarnings("unchecked")
    public R invoke(T thisRef, P1 arg1, P2 arg2, P3 arg3, P4 arg4, P5 arg5) {
        try {
            return (R) methodHandle.invoke(thisRef, arg1, arg2, arg3, arg4, arg5);
        } catch (Throwable throwable) {
            throw new UncheckedReflectionException(String.format("Could not invoke %s [%s] on %s with args %s, %s, %s and %s", name, methodHandle, thisRef, arg1, arg2, arg3, arg4), throwable);
        }
    }
}
