package ladysnake.reflectivefabric.reflection.typed;

import ladysnake.reflectivefabric.reflection.UncheckedReflectionException;
import org.apiguardian.api.API;

import java.lang.invoke.MethodHandle;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL, since = "3.0.0")
public class TypedStaticMethod2<P1, P2, R> extends TypedStaticMethod {
    public TypedStaticMethod2(MethodHandle methodHandle, String name, Class<?> declaringClass) {
        super(methodHandle, name, declaringClass);
    }

    @SuppressWarnings("unchecked")
    public R invoke(P1 p1, P2 p2) {
        try {
            return (R) methodHandle.invoke(p1, p2);
        } catch (Throwable throwable) {
            throw new UncheckedReflectionException(String.format("Could not invoke %s [%s] on %s", name, methodHandle, declaringClass), throwable);
        }
    }
}
