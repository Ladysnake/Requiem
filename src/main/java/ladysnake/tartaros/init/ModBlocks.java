package ladysnake.tartaros.init;

import ladysnake.tartaros.blocks.BlockCrystallizer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static BlockCrystallizer crystallizer;

    public static void init() {
    	crystallizer = new BlockCrystallizer();
    }
    
    public static void register() {
    	registerBlock(crystallizer);
    }
    
    private static void registerBlock(Block block) {
    	GameRegistry.register(block);
    	ItemBlock item = new ItemBlock(block);
    	item.setRegistryName(block.getRegistryName());
    	GameRegistry.register(item);
    }
    
    public static void registerRenders() {
    	registerRender(crystallizer);
    }
    
    private static void registerRender(Block block) {
    	Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }
}
