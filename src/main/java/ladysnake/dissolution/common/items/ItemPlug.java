package ladysnake.dissolution.common.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class ItemPlug extends Item implements ICustomLocation {
    @Override
    public ModelResourceLocation getModelLocation() {
        assert this.getRegistryName() != null;
        return new ModelResourceLocation(this.getRegistryName().getResourceDomain() + ":machines/" + this.getRegistryName().getResourcePath());
    }
}
