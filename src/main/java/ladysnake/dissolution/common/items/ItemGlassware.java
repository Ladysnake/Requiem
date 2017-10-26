package ladysnake.dissolution.common.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class ItemGlassware extends Item implements ICustomLocation {
    @Override
    public ModelResourceLocation getModelLocation() {
        return new ModelResourceLocation(this.getRegistryName().getResourceDomain() + ":glassware/" + this.getRegistryName().getResourcePath());
    }
}
