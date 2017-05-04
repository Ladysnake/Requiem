package ladysnake.tartaros.common.init;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.blocks.BlockCrystallizer;
import ladysnake.tartaros.common.blocks.BlockEctoplasm;
import ladysnake.tartaros.common.blocks.BlockMercuriusWaystone;
import ladysnake.tartaros.common.blocks.BlockSepulture;
import ladysnake.tartaros.common.blocks.BlockSoulAnchor;
import ladysnake.tartaros.common.blocks.BlockSoulCandle;
import ladysnake.tartaros.common.blocks.BlockSoulExtractor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {

	public static Block ECTOPLASMA;
	public static BlockEctoplasm ECTOPLASM;
    public static BlockCrystallizer CRYSTALLIZER;
    public static BlockMercuriusWaystone MERCURIUS_WAYSTONE;
    public static BlockSoulAnchor SOUL_ANCHOR;
    public static BlockSoulCandle SOUL_CANDLE;
    public static BlockSoulExtractor SOUL_EXTRACTOR;
    public static BlockSepulture SEPULTURE;

    public static void init() {
    	CRYSTALLIZER = new BlockCrystallizer();
    	ECTOPLASM = new BlockEctoplasm();
    	ECTOPLASMA = new Block(Material.CLOTH);
    	ECTOPLASMA.setUnlocalizedName(Reference.Blocks.ECTOPLASMA.getUnlocalizedName());
    	ECTOPLASMA.setRegistryName(Reference.Blocks.ECTOPLASMA.getRegistryName());
    	ECTOPLASMA.setHardness(0.5f);
    	MERCURIUS_WAYSTONE = new BlockMercuriusWaystone();
    	SOUL_ANCHOR = new BlockSoulAnchor();
    	SOUL_CANDLE = new BlockSoulCandle();
    	SOUL_EXTRACTOR = new BlockSoulExtractor();
    	SEPULTURE = new BlockSepulture();
    }
    
    public static void register() {
    	registerBlock(CRYSTALLIZER);
    	registerBlock(ECTOPLASMA);
    	registerBlock(ECTOPLASM);
    	registerBlock(MERCURIUS_WAYSTONE).setMaxStackSize(1);
    	registerBlock(SOUL_ANCHOR);
    	registerBlock(SOUL_CANDLE);
    	GameRegistry.register(SEPULTURE);
    	registerBlock(SOUL_EXTRACTOR);
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
    	registerRender(CRYSTALLIZER);
    	registerRender(SOUL_EXTRACTOR);
    	registerRender(MERCURIUS_WAYSTONE);
    	registerRender(SEPULTURE);
    	registerRender(ECTOPLASM);
    	registerRender(ECTOPLASMA);
    	registerRender(SOUL_ANCHOR);
    	registerRender(SOUL_CANDLE);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRender(Block block) {
    	ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(Reference.MOD_ID + ":" + block.getUnlocalizedName().toString().substring(5)));
    }
}
