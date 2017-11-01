package ladysnake.dissolution.api;

import java.util.Map;

public interface IGenericInventoryProvider extends Iterable<Map.Entry<Class, GenericStackInventory>> {

    default boolean hasInventoryFor(Object o) {
        return hasInventoryFor(o.getClass());
    }

    boolean hasInventoryFor(Class clazz);

    <T> GenericStackInventory<T> getInventoryFor(Class<T> clazz);

    <T> void setInventory(Class<T> clazz, GenericStackInventory<T> inventory);

}
