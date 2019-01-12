package ladysnake.reflectivefabric.reflection.typed;

import ladysnake.reflectivefabric.reflection.UncheckedReflectionException;
import org.apiguardian.api.API;

import java.lang.invoke.MethodHandle;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL, since = "2.6.2")
public class TypedMethod0<T, R> extends TypedMethod {
    public TypedMethod0(MethodHandle methodHandle, String name) {
        super(methodHandle, name);
    }

    @SuppressWarnings("unchecked")
    public R invoke(T thisRef) {
        try {
            return (R) methodHandle.invoke(thisRef);
        } catch (Throwable throwable) {
            throw new UncheckedReflectionException(String.format("Could not invoke %s [%s] on %s", name, methodHandle, thisRef), throwable);
        }
    }
}
