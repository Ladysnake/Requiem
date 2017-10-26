package ladysnake.dissolution.common.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

@SuppressWarnings("ConstantConditions")
public class ItemMineral extends Item implements ICustomLocation {
    @Override
    public ModelResourceLocation getModelLocation() {
        return new ModelResourceLocation(this.getRegistryName().getResourceDomain() + ":mineral/" + this.getRegistryName().getResourcePath());
    }
}
