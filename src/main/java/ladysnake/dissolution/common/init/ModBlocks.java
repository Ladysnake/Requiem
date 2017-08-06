package ladysnake.dissolution.common.init;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.BlockBaseMachine;
import ladysnake.dissolution.common.blocks.BlockCrystallizer;
import ladysnake.dissolution.common.blocks.BlockEctoplasm;
import ladysnake.dissolution.common.blocks.BlockMercuriusWaystone;
import ladysnake.dissolution.common.blocks.BlockMercuryCandle;
import ladysnake.dissolution.common.blocks.BlockPowerCable;
import ladysnake.dissolution.common.blocks.BlockPowerCore;
import ladysnake.dissolution.common.blocks.BlockSepulture;
import ladysnake.dissolution.common.blocks.BlockSoulAnchor;
import ladysnake.dissolution.common.blocks.BlockSoulExtractor;
import ladysnake.dissolution.common.blocks.BlockSulfurCandle;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import scala.actors.threadpool.Arrays;

public final class ModBlocks {
	
	/**Used to register stuff*/
	static final ModBlocks INSTANCE = new ModBlocks();

	public static Block ECTOPLASMA;
	public static BlockBaseMachine BASE_MACHINE;
    public static BlockCrystallizer CRYSTALLIZER;
	public static BlockEctoplasm ECTOPLASM;
	public static BlockPowerCable POWER_CABLE;
	public static BlockPowerCore POWER_CORE;
    public static BlockMercuriusWaystone MERCURIUS_WAYSTONE;
    public static BlockMercuryCandle MERCURY_CANDLE;
    public static BlockSepulture SEPULTURE;
    public static BlockSoulAnchor SOUL_ANCHOR;
    public static BlockSoulExtractor SOUL_EXTRACTOR;
    public static BlockSulfurCandle SULFUR_CANDLE;
    
    static Set<Block> allBlocks = new HashSet<>();
    
    private IForgeRegistry<Block> blockRegistry;
    
    private <T extends Block> T giveNames(T block, Reference.Blocks names) {
		return (T) block.setUnlocalizedName(names.getUnlocalizedName()).setRegistryName(names.getRegistryName());
	}

    @SubscribeEvent
    public void onRegister(RegistryEvent.Register<Block> event) {
    	allBlocks.clear();
    	blockRegistry = event.getRegistry();
    	registerBlocks(
    			BASE_MACHINE = giveNames(new BlockBaseMachine(), Reference.Blocks.BASE_MACHINE),
    			CRYSTALLIZER = giveNames(new BlockCrystallizer(), Reference.Blocks.CRYSTALLIZER),
    			ECTOPLASMA = giveNames(new Block(Material.CLOTH).setHardness(0.5f), Reference.Blocks.ECTOPLASMA),
    			ECTOPLASM = giveNames(new BlockEctoplasm(), Reference.Blocks.ECTOPLASM),
    			MERCURY_CANDLE = giveNames(new BlockMercuryCandle(), Reference.Blocks.MERCURY_CANDLE),
    			POWER_CABLE = giveNames(new BlockPowerCable(), Reference.Blocks.POWER_CABLE),
    			POWER_CORE = giveNames(new BlockPowerCore(), Reference.Blocks.POWER_CORE),
    			SOUL_ANCHOR = giveNames(new BlockSoulAnchor(), Reference.Blocks.SOUL_ANCHOR), 
    			SULFUR_CANDLE = giveNames(new BlockSulfurCandle(), Reference.Blocks.SULFUR_CANDLE), 
    			SOUL_EXTRACTOR = giveNames(new BlockSoulExtractor(), Reference.Blocks.SOUL_EXTRACTOR));
    	registerBlock(MERCURIUS_WAYSTONE = giveNames(new BlockMercuriusWaystone(), Reference.Blocks.MERCURIUS_WAYSTONE), true).setMaxStackSize(1);
    	blockRegistry.register(SEPULTURE = giveNames(new BlockSepulture(), Reference.Blocks.SEPULTURE));
    }
    
    void registerBlocks(Block... blocks) {
    	for(Block b : blocks)
    		registerBlock(b, true);
    }
    
    Item registerBlock(Block block, boolean addToTab) {
    	allBlocks.add(block);
    	blockRegistry.register(block);
    	ItemBlock item = new ItemBlock(block);
    	item.setRegistryName(block.getRegistryName());
    	ModItems.allItems.add(item);
    	if(addToTab)
    		block.setCreativeTab(Dissolution.CREATIVE_TAB);
    	return item;
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerRenders(ModelRegistryEvent event) {
    	allBlocks.forEach(this::registerRender);
    }
    
    @SideOnly(Side.CLIENT)
    private void registerRender(Block block) {
    	ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(Reference.MOD_ID + ":" + block.getUnlocalizedName().toString().substring(5)));
    }
    
    private ModBlocks() {}
}
