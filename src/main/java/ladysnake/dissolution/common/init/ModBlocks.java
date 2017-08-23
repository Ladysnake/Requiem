package ladysnake.dissolution.common.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ladysnake.dissolution.client.models.blocks.CableBakedModel;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.BlockCrystallizer;
import ladysnake.dissolution.common.blocks.BlockEctoplasm;
import ladysnake.dissolution.common.blocks.BlockMercuriusWaystone;
import ladysnake.dissolution.common.blocks.BlockMercuryCandle;
import ladysnake.dissolution.common.blocks.BlockSepulture;
import ladysnake.dissolution.common.blocks.BlockSoulAnchor;
import ladysnake.dissolution.common.blocks.BlockSulfurCandle;
import ladysnake.dissolution.common.blocks.powersystem.BlockBarrage;
import ladysnake.dissolution.common.blocks.powersystem.BlockBaseMachine;
import ladysnake.dissolution.common.blocks.powersystem.BlockPowerCable;
import ladysnake.dissolution.common.blocks.powersystem.BlockPowerCore;
import ladysnake.dissolution.common.blocks.powersystem.BlockSoulExtractor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModBlocks {
	
	/**Used to register stuff*/
	static final ModBlocks INSTANCE = new ModBlocks();

	public static Block ECTOPLASMA;
	public static BlockBaseMachine BASE_MACHINE;
	public static BlockBarrage BARRAGE;
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
    
    @SideOnly(Side.CLIENT)
    Map<Block, ModelResourceLocation> specialRenderBlocks = new HashMap<>();
	Map<String, Block> remaps = new HashMap<>();
    
    private IForgeRegistry<Block> blockRegistry;
    
    private static <T extends Block> T giveNames(T block, Reference.Blocks names) {
		return (T) block.setUnlocalizedName(names.getUnlocalizedName()).setRegistryName(names.getRegistryName());
	}

    @SubscribeEvent
    public void onRegister(RegistryEvent.Register<Block> event) {
    	blockRegistry = event.getRegistry();
    	registerBlocks(
    			BASE_MACHINE = giveNames(new BlockBaseMachine(), Reference.Blocks.BASE_MACHINE),
    			BARRAGE = giveNames(new BlockBarrage(), Reference.Blocks.BARRAGE),
    			CRYSTALLIZER = giveNames(new BlockCrystallizer(), Reference.Blocks.CRYSTALLIZER),
    			ECTOPLASMA = giveNames(new Block(Material.CLOTH).setHardness(0.5f), Reference.Blocks.ECTOPLASMA),
    			ECTOPLASM = giveNames(new BlockEctoplasm(), Reference.Blocks.ECTOPLASM),
    			MERCURY_CANDLE = giveNames(new BlockMercuryCandle(), Reference.Blocks.MERCURY_CANDLE),
    			POWER_CABLE = giveNames(new BlockPowerCable(), Reference.Blocks.POWER_CABLE),
    			POWER_CORE = giveNames(new BlockPowerCore(), Reference.Blocks.POWER_CORE),
    			SOUL_ANCHOR = giveNames(new BlockSoulAnchor(), Reference.Blocks.SOUL_ANCHOR), 
    			SULFUR_CANDLE = giveNames(new BlockSulfurCandle(), Reference.Blocks.SULFUR_CANDLE), 
    			SOUL_EXTRACTOR = giveNames(new BlockSoulExtractor(), Reference.Blocks.SOUL_EXTRACTOR));
    	registerBlock(MERCURIUS_WAYSTONE = giveNames(new BlockMercuriusWaystone(), Reference.Blocks.MERCURIUS_WAYSTONE)).setMaxStackSize(1);
    	blockRegistry.register(SEPULTURE = giveNames(new BlockSepulture(), Reference.Blocks.SEPULTURE));
    }
    
    void registerBlocks(Block... blocks) {
    	for(Block b : blocks)
    		registerBlock(b);
    }
    
    Item registerBlock(Block block) {
    	return registerBlock(block, true);
    }
    
    Item registerBlock(Block block, boolean addToTab) {
    	blockRegistry.register(block);
    	Item item = new ItemBlock(block).setRegistryName(block.getRegistryName());
    	ModItems.allItems.add(item);
    	if(addToTab)
    		block.setCreativeTab(Dissolution.CREATIVE_TAB);
    	return item;
    }
    
    @SubscribeEvent
    public void remapIds(RegistryEvent.MissingMappings<Block> event) {
    	List<Mapping<Block>> missingBlocks = event.getMappings();
    	remaps.put("blockcrystallizer", CRYSTALLIZER);
    	remaps.put("blockectoplasm", ECTOPLASM);
    	remaps.put("blockectoplasma", ECTOPLASMA);
    	remaps.put("blockmercuriuswaystone", MERCURIUS_WAYSTONE);
    	remaps.put("blockmercurycandle", MERCURY_CANDLE);
    	remaps.put("blocksepulture", SEPULTURE);
    	remaps.put("blocksoulanchor", SOUL_ANCHOR);
    	remaps.put("blocksoulextractor", SOUL_EXTRACTOR);
    	remaps.put("blocksulfurcandle", SULFUR_CANDLE);
    	for(Mapping<Block> map : missingBlocks) {
    		if(map.key.getResourceDomain().equals(Reference.MOD_ID)) {
    			if(remaps.get(map.key.getResourcePath()) != null)
    				map.remap(remaps.get(map.key.getResourcePath()));
    		}
    	}
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerRenders(ModelRegistryEvent event) {
    	registerSmartRender(POWER_CABLE, CableBakedModel.BAKED_MODEL);
    }
    
    @SideOnly(Side.CLIENT)
    private void registerSmartRender(Block block, ModelResourceLocation rl) {
    	StateMapperBase ignoreState = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return rl;
            }
        };
        ModelLoader.setCustomStateMapper(block, ignoreState);
    }
    
    private ModBlocks() {}
}
