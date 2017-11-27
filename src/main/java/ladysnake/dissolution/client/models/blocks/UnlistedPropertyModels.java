package ladysnake.dissolution.client.models.blocks;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Set;

public class UnlistedPropertyModels implements IUnlistedProperty<Set> {

    @Override
    public String getName() {
        return "AlchemyModules";
    }

    @Override
    public boolean isValid(Set value) {
        return value.stream().allMatch(v -> v instanceof ResourceLocation);
    }

    @Override
    public Class<Set> getType() {
        return Set.class;
    }

    @Override
    public String valueToString(Set value) {
        return "[Alchemy Module: " + value + ", " + value + "]";
    }

}
