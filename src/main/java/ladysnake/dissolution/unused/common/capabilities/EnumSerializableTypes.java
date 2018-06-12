package ladysnake.dissolution.unused.common.capabilities;

import ladysnake.dissolution.unused.api.DistillateTypes;
import ladysnake.dissolution.unused.api.INBTSerializableType;
import ladysnake.dissolution.unused.common.EnumPowderOres;

public enum EnumSerializableTypes {
    DISTILLATE(DistillateTypes.class, DistillateTypes.SERIALIZER),
    POWDER(EnumPowderOres.class, EnumPowderOres.SERIALIZER);

    public final Class clazz;
    public final INBTSerializableType.INBTTypeSerializer serializer;

    <T> EnumSerializableTypes(Class<T> clazz, INBTSerializableType.INBTTypeSerializer<T> serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    public static EnumSerializableTypes forClass(Class clazz) {
        for (EnumSerializableTypes type : values()) {
            if (type.clazz.equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException("The provided class is not a registered entry");
    }
}
