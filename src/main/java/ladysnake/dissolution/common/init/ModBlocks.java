package ladysnake.dissolution.common.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ladysnake.dissolution.client.models.blocks.CableBakedModel;
import ladysnake.dissolution.client.models.blocks.EssentiaPipeBakedModel;
import ladysnake.dissolution.client.models.blocks.ModularMachineBakedModel;
import ladysnake.dissolution.client.models.DissolutionModelLoader;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.BlockDepletedClay;
import ladysnake.dissolution.common.blocks.BlockSepulture;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockBarrage;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockEssentiaPipe;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockPowerCable;
import ladysnake.dissolution.common.blocks.BlockDepleted;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
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

import javax.annotation.Nonnull;

@SuppressWarnings("WeakerAccess")
public final class ModBlocks {
	
	/**Used to register stuff*/
	static final ModBlocks INSTANCE = new ModBlocks();

	public static Block CINNABAR;
	public static Block HALITE;
	public static Block SULPHUR;
	public static BlockBarrage BARRAGE;
	public static BlockCasing CASING;
	public static BlockEssentiaPipe ESSENTIA_PIPE;
	public static BlockDepleted DEPLETED_CLAY;
	public static BlockDepleted DEPLETED_COAL;
	public static BlockDepleted DEPLETED_MAGMA;
	public static BlockPowerCable POWER_CABLE;
    public static BlockSepulture SEPULTURE;
    
	Map<String, Block> remaps = new HashMap<>();
    
    @SuppressWarnings("unchecked")
	private static <T extends Block> T name(T block, Reference.Blocks names) {
		return (T) block.setUnlocalizedName(names.getUnlocalizedName()).setRegistryName(names.getRegistryName());
	}
    
    @SuppressWarnings("unchecked")
	private static <T extends Block> T name(T block, String name) {
    	return (T) block.setUnlocalizedName(name).setRegistryName(name);
    }

    @SubscribeEvent
    public void onRegister(RegistryEvent.Register<Block> event) {
    	IForgeRegistry<Block> blockRegistry = event.getRegistry();
    	registerBlocks(blockRegistry,
    			CINNABAR = name(new Block(Material.ROCK), "cinnabar_block"),
				HALITE = name(new Block(Material.ROCK), "halite_block"),
				SULPHUR = name(new Block(Material.ROCK), "sulfur_block"),
    			BARRAGE = name(new BlockBarrage(), Reference.Blocks.BARRAGE),
    			DEPLETED_CLAY = name(new BlockDepletedClay(), "depleted_clay_block"),
    			DEPLETED_COAL = name(new BlockDepleted(Material.ROCK), "depleted_coal_block"),
    			DEPLETED_MAGMA = name(new BlockDepleted(Material.ROCK), "depleted_magma"),
    			ESSENTIA_PIPE = name(new BlockEssentiaPipe(), "essentia_pipe"),
    			POWER_CABLE = name(new BlockPowerCable(), Reference.Blocks.POWER_CABLE));
    	blockRegistry.register(CASING = name(new BlockCasing(), "wooden_casing"));
    	blockRegistry.register(SEPULTURE = name(new BlockSepulture(), Reference.Blocks.SEPULTURE));
    }
    
    private void registerBlocks(IForgeRegistry<Block> blockRegistry, Block... blocks) {
    	for(Block b : blocks)
    		registerBlock(blockRegistry, b);
    }
    
    private void registerBlock(IForgeRegistry<Block> blockRegistry, Block block) {
    	registerBlock(blockRegistry, block, true);
    }
    
    @SuppressWarnings("UnusedReturnValue")
	Item registerBlock(IForgeRegistry<Block> blockRegistry, Block block, boolean addToTab) {
    	blockRegistry.register(block);
    	assert block.getRegistryName() != null;
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
    	ItemAlchemyModule.registerModels();
    	DissolutionModelLoader.addModel(BlockCasing.PLUG, ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
    	DissolutionModelLoader.addAllModels(BlockCasing.CASING_BOTTOM, BlockCasing.CASING_TOP, CableBakedModel.INTERSECTION,
				EssentiaPipeBakedModel.INTERSECTION);
		DissolutionModelLoader.addModel(CableBakedModel.START, ModelRotation.X0_Y90,
				ModelRotation.X0_Y180, ModelRotation.X0_Y270, ModelRotation.X90_Y0, ModelRotation.X270_Y0);
		DissolutionModelLoader.addModel(EssentiaPipeBakedModel.START, ModelRotation.X0_Y90,
				ModelRotation.X0_Y180, ModelRotation.X0_Y270, ModelRotation.X90_Y0, ModelRotation.X270_Y0);
		DissolutionModelLoader.addModel(CableBakedModel.SECTION, ModelRotation.X90_Y0, ModelRotation.X0_Y90);
		DissolutionModelLoader.addModel(EssentiaPipeBakedModel.SECTION, ModelRotation.X90_Y0, ModelRotation.X0_Y90);
    	registerSmartRender(POWER_CABLE, CableBakedModel.BAKED_MODEL);
    	registerSmartRender(ESSENTIA_PIPE, EssentiaPipeBakedModel.BAKED_MODEL);
    	registerSmartRender(CASING, ModularMachineBakedModel.BAKED_MODEL);
    }
    
    @SideOnly(Side.CLIENT)
    private void registerSmartRender(Block block, ModelResourceLocation rl) {
    	StateMapperBase ignoreState = new StateMapperBase() {
            @Nonnull
			@Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState iBlockState) {
                return rl;
            }
        };
        ModelLoader.setCustomStateMapper(block, ignoreState);
    }
    
    private ModBlocks() {}
}
