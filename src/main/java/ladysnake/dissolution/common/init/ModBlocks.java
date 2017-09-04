package ladysnake.dissolution.common.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ladysnake.dissolution.client.models.blocks.CableBakedModel;
import ladysnake.dissolution.client.models.blocks.ModularMachineBakedModel;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.BlockSepulture;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockBarrage;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockBaseMachine;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockPowerCable;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockPowerCore;
import net.minecraft.block.Block;
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

	public static BlockCasing CASING;
	public static BlockBarrage BARRAGE;
	public static BlockPowerCable POWER_CABLE;
	public static BlockPowerCore POWER_CORE;
    public static BlockSepulture SEPULTURE;
    
    @SideOnly(Side.CLIENT)
    Map<Block, ModelResourceLocation> specialRenderBlocks = new HashMap<>();
	Map<String, Block> remaps = new HashMap<>();
    
    private IForgeRegistry<Block> blockRegistry;
    
    private static <T extends Block> T name(T block, Reference.Blocks names) {
		return (T) block.setUnlocalizedName(names.getUnlocalizedName()).setRegistryName(names.getRegistryName());
	}
    
    private static <T extends Block> T name(T block, String name) {
    	return (T) block.setUnlocalizedName(name).setRegistryName(name);
    }

    @SubscribeEvent
    public void onRegister(RegistryEvent.Register<Block> event) {
    	blockRegistry = event.getRegistry();
    	registerBlocks(
    			BARRAGE = name(new BlockBarrage(), Reference.Blocks.BARRAGE),
    			POWER_CABLE = name(new BlockPowerCable(), Reference.Blocks.POWER_CABLE),
    			POWER_CORE = name(new BlockPowerCore(), Reference.Blocks.POWER_CORE));
    	blockRegistry.register(CASING = name(new BlockCasing(), "wooden_casing"));
    	blockRegistry.register(SEPULTURE = name(new BlockSepulture(), Reference.Blocks.SEPULTURE));
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
    	remaps.put("blocksepulture", SEPULTURE);
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
    	registerSmartRender(CASING, ModularMachineBakedModel.BAKED_MODEL);
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
