package ladysnake.dissolution.common.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ICustomLocation {

    ModelResourceLocation getModelLocation();

    @SideOnly(Side.CLIENT)
    default void registerRender() {
        ModelLoader.setCustomModelResourceLocation((Item) this, 0, getModelLocation());
    }

}
