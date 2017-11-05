package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.INBTSerializableType;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.tileentities.TileEntityMortar;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public enum EnumPowderOres {
    CINNABAR(ModItems.CINNABAR),
    HALITE(ModItems.HALITE),
    SULFUR(ModItems.SULFUR),
    CLAY(Items.CLAY_BALL, HALITE, ModItems.DEPLETED_CLAY),
    COAL(Items.COAL, SULFUR, ModItems.DEPLETED_COAL),
    MAGMA(ModItems.MAGMA_STONE, CINNABAR, ModItems.IGNEOUS_ROCK);

    private final Item component;
    private final EnumPowderOres refinedPowder;
    private final Item depletedResidues;

    EnumPowderOres(Item component) {
        this(component, null, Items.AIR);
    }

    EnumPowderOres(Item component, EnumPowderOres refined, Item depleted) {
        this.component = component;
        this.refinedPowder = refined;
        this.depletedResidues = depleted;
    }

    public Item getComponent() {
        return component;
    }

    public EnumPowderOres getRefinedPowder() {
        return refinedPowder;
    }

    public Item getDepletedResidues() {
        return depletedResidues;
    }

    public static final INBTSerializableType.INBTTypeSerializer<EnumPowderOres> SERIALIZER = new INBTSerializableType.EnumNBTTypeSerializer<>(EnumPowderOres.class);

}
