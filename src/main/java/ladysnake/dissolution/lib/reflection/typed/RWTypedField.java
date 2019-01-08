package ladysnake.dissolution.lib.reflection.typed;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * A read-write reference to a field, using method handles as getter and setter
 * @param <T> type of the field's owner
 * @param <R> type of the field's value
 * @see TypedMethodHandles#createFieldRef(Class, String, Class)
 * @since 2.6
 */
@API(status = EXPERIMENTAL, since = "2.6.2")
public class RWTypedField<T, R> {
    private TypedGetter<T, R> getter;
    private TypedSetter<T, R> setter;

    public RWTypedField(TypedGetter<T, R> getter, TypedSetter<T, R> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public R get(T thisRef) {
        return this.getter.get(thisRef);
    }

    public void set(T thisRef, R value) {
        this.setter.set(thisRef, value);
    }
}
