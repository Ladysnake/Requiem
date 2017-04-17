package ladysnake.tartaros.common.init;

import ladysnake.tartaros.common.blocks.BlockCrystallizer;
import ladysnake.tartaros.common.blocks.BlockSoulExtractor;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {

    public static BlockCrystallizer crystallizer;
    public static BlockSoulExtractor soul_extractor;

    public static void init() {
    	crystallizer = new BlockCrystallizer();
    	soul_extractor = new BlockSoulExtractor();
    }
    
    public static void register() {
    	registerBlock(crystallizer);
    	registerBlock(soul_extractor);
    }
    
    private static void registerBlock(Block block) {
    	GameRegistry.register(block);
    	ItemBlock item = new ItemBlock(block);
    	item.setRegistryName(block.getRegistryName());
    	GameRegistry.register(item);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
    	registerRender(crystallizer);
    	registerRender(soul_extractor);
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerRender(Block block) {
    	Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }
}
