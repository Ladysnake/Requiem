package ladysnake.tartaros.common.init;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.blocks.BlockCrystallizer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {
	public static Item ectoplasm, ectoplasma;

    public static void init() {
    	ectoplasm = new Item();
    	ectoplasm.setUnlocalizedName(Reference.Items.ECTOPLASM.getUnlocalizedName());
        ectoplasm.setRegistryName(Reference.Items.ECTOPLASM.getRegistryName());
        ectoplasma = new Item();
    	ectoplasma.setUnlocalizedName(Reference.Items.ECTOPLASMA.getUnlocalizedName());
        ectoplasma.setRegistryName(Reference.Items.ECTOPLASMA.getRegistryName());
    }
    
    public static void register() {
    	GameRegistry.register(ectoplasm);
    	GameRegistry.register(ectoplasma);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
    	registerRender(ectoplasm);
    	registerRender(ectoplasma);
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerRender(Item item) {
    	Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
