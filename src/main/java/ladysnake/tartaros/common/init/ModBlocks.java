package ladysnake.tartaros.common.init;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.blocks.BlockCrystallizer;
import ladysnake.tartaros.common.blocks.BlockEctoplasm;
import ladysnake.tartaros.common.blocks.BlockMercuriusWaystone;
import ladysnake.tartaros.common.blocks.BlockSepulture;
import ladysnake.tartaros.common.blocks.BlockSoulAnchor;
import ladysnake.tartaros.common.blocks.BlockSoulExtractor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {

	public static Block ectoplasma;
	public static BlockEctoplasm ectoplasm;
    public static BlockCrystallizer crystallizer;
    public static BlockMercuriusWaystone mercurius_waystone;
    public static BlockSoulAnchor soul_anchor;
    public static BlockSoulExtractor soul_extractor;
    public static BlockSepulture sepulture;

    public static void init() {
    	crystallizer = new BlockCrystallizer();
    	ectoplasm = new BlockEctoplasm();
    	ectoplasma = new Block(Material.CLOTH);
    	ectoplasma.setUnlocalizedName(Reference.Blocks.ECTOPLASMA.getUnlocalizedName());
    	ectoplasma.setRegistryName(Reference.Blocks.ECTOPLASMA.getRegistryName());
    	ectoplasma.setHardness(0.5f);
    	mercurius_waystone = new BlockMercuriusWaystone();
    	soul_anchor = new BlockSoulAnchor();
    	soul_extractor = new BlockSoulExtractor();
    	sepulture = new BlockSepulture();
    }
    
    public static void register() {
    	registerBlock(crystallizer);
    	registerBlock(ectoplasma);
    	registerBlock(ectoplasm);
    	registerBlock(mercurius_waystone).setMaxStackSize(1);
    	GameRegistry.register(sepulture);
    	registerBlock(soul_extractor);
    	registerBlock(soul_anchor);
    }
    
    private static Item registerBlock(Block block) {
    	GameRegistry.register(block);
    	ItemBlock item = new ItemBlock(block);
    	item.setRegistryName(block.getRegistryName());
    	GameRegistry.register(item);
    	block.setCreativeTab(Tartaros.CREATIVE_TAB);
    	return item;
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
    	registerRender(crystallizer);
    	registerRender(soul_extractor);
    	registerRender(mercurius_waystone);
    	registerRender(sepulture);
    	registerRender(ectoplasm);
    	registerRender(ectoplasma);
    	registerRender(soul_anchor);
    }
    
    @SideOnly(Side.CLIENT)
    public static String modid = Reference.MOD_ID;

    public static void registerRender(Block block) {
    	ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(Reference.MOD_ID + ":" + block.getUnlocalizedName().toString().substring(5)));
    }
}
