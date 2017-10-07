package ladysnake.dissolution.client.models.blocks;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyResourceLocation implements IUnlistedProperty<ResourceLocation> {
    private final String name;

    public PropertyResourceLocation(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(ResourceLocation value) {
        return true;
    }

    @Override
    public Class<ResourceLocation> getType() {
        return ResourceLocation.class;
    }

    @Override
    public String valueToString(ResourceLocation value) {
        return value.toString();
    }
}
