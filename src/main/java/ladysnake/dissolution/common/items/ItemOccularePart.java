package ladysnake.dissolution.common.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class ItemOccularePart extends Item implements ICustomLocation {

    private static int currentId;

    private final int id;

    public ItemOccularePart() {
        this(0);
    }

    public ItemOccularePart(int durability) {
        super();
        this.id = currentId++;
        this.setMaxDamage(durability);
    }

    public int getId() {
        return id;
    }

    @Override
    public ModelResourceLocation getModelLocation() {
        assert this.getRegistryName() != null;
        return new ModelResourceLocation(this.getRegistryName().getResourceDomain() + ":occularia_parts/" + this.getRegistryName().getResourcePath());
    }
}
