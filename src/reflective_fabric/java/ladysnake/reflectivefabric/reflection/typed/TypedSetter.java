package ladysnake.reflectivefabric.reflection.typed;

import org.apiguardian.api.API;

import java.lang.invoke.MethodHandle;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * A typed setter using a method handle to access a field
 * @param <T> type of the field's owner
 * @param <P1> type of the field's value
 *
 * @since 2.6
 */
@API(status = EXPERIMENTAL, since = "2.6.2")
public class TypedSetter<T, P1> extends TypedMethod1<T, P1, Void> {
    public TypedSetter(MethodHandle methodHandle, String name) {
        super(methodHandle, name);
    }

    public void set(T thisRef, P1 value) {
        this.invoke(thisRef, value);
    }
}
