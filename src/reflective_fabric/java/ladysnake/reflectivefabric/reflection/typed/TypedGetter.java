package ladysnake.reflectivefabric.reflection.typed;

import org.apiguardian.api.API;

import java.lang.invoke.MethodHandle;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * A typed getter using a method handle to access a field
 * @param <T> type of the field's owner
 * @param <R> type of the field's value
 *
 * @since 2.6
 */
@API(status = EXPERIMENTAL, since = "2.6.2")
public class TypedGetter<T, R> extends TypedMethod0<T, R> {
    public TypedGetter(MethodHandle methodHandle, String name) {
        super(methodHandle, name);
    }

    public R get(T thisRef) {
        return this.invoke(thisRef);
    }
}
