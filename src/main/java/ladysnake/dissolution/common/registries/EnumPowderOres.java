package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.INBTSerializableType;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.tileentities.TileEntityMortar;
import net.minecraft.item.Item;

public enum EnumPowderOres {
    SULFUR(ModItems.SULFUR),
    CINNABAR(ModItems.CINNABAR),
    HALITE(ModItems.HALITE);

    private final Item component;

    EnumPowderOres(Item component) {
        this.component = component;
    }

    public Item getComponent() {
        return component;
    }

    public static final INBTSerializableType.INBTTypeSerializer<EnumPowderOres> SERIALIZER = new INBTSerializableType.EnumNBTTypeSerializer<>(EnumPowderOres.class);

}
