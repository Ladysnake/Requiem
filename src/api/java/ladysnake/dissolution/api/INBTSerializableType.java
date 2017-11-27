package ladysnake.dissolution.api;

import net.minecraft.nbt.NBTTagCompound;

/**
 * An interface defining a type that can be externally serialized to NBT
 *
 * @param <T> usually the type itself
 */
public interface INBTSerializableType<T> {

    INBTTypeSerializer<T> getSerializer();

    interface INBTTypeSerializer<T> {

        void serialize(T value, NBTTagCompound compound);

        T deserialize(NBTTagCompound compound);

    }

    class EnumNBTTypeSerializer<T extends Enum<T>> implements INBTTypeSerializer<T> {

        private Class<T> enumClass;

        public EnumNBTTypeSerializer(Class<T> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        public void serialize(T value, NBTTagCompound compound) {
            if (value != null)
                compound.setString("type", value.name());
        }

        @Override
        public T deserialize(NBTTagCompound compound) {
            try {
                return Enum.valueOf(enumClass, compound.getString("type"));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}