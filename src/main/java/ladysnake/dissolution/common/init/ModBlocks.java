package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.*;
import ladysnake.dissolution.common.items.ItemSoulInAJar;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings("WeakerAccess")
public final class ModBlocks {

    /**
     * Used to register stuff
     */
    static final ModBlocks INSTANCE = new ModBlocks();

    public static BlockCrucible CRUCIBLE;
    public static BlockLamentStone LAMENT_STONE;
    public static BlockMortar MORTAR;
    public static BlockSepulture SEPULTURE;
    public static BlockShrine SHRINE;
    public static BlockWisp WISP_IN_A_JAR;

    Map<String, Block> remaps = new HashMap<>();

    @Nonnull
    @SuppressWarnings("unchecked")
    static <T extends Block> T name(T block, String name) {
        return (T) block.setUnlocalizedName(name).setRegistryName(new ResourceLocation(Reference.MOD_ID, name));
    }

    @SubscribeEvent
    public void onRegister(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> blockRegistry = event.getRegistry();
        registerBlocks(blockRegistry,
                SHRINE = name(new BlockShrine(), "passeress_shrine"));
        ModItems.SOUL_IN_A_JAR = registerBlock(blockRegistry, WISP_IN_A_JAR = name(new BlockWisp(), "wisp_jar"), true,
                block -> (ItemSoulInAJar) new ItemSoulInAJar(block).setRegistryName("wisp_in_a_jar"));
        blockRegistry.register(SEPULTURE = name(new BlockSepulture(), "stone_burial"));
    }

    private void registerBlocks(IForgeRegistry<Block> blockRegistry, Block... blocks) {
        for (Block b : blocks)
            registerBlock(blockRegistry, b);
    }

    private void registerBlock(IForgeRegistry<Block> blockRegistry, Block block) {
        registerBlock(blockRegistry, block, true, ((Function<Block, ItemBlock>)(ItemBlock::new)).andThen(item -> item.setRegistryName(Objects.requireNonNull(block.getRegistryName()))));
    }

    @SuppressWarnings("unchecked")
    <T extends Item> T registerBlock(IForgeRegistry<Block> blockRegistry, Block block, boolean addToTab, Function<Block, T> blockItemFunction) {
        blockRegistry.register(block);
        assert block.getRegistryName() != null;
        T item = blockItemFunction.apply(block);
        ModItems.allItems.add(item);
        if (addToTab)
            block.setCreativeTab(Dissolution.CREATIVE_TAB);
        return item;
    }

    @SubscribeEvent
    public void remapIds(RegistryEvent.MissingMappings<Block> event) {
        List<Mapping<Block>> missingBlocks = event.getMappings();
        remaps.put("blocksepulture", SEPULTURE);

        for (Mapping<Block> map : missingBlocks) {
            if (map.key.getResourceDomain().equals(Reference.MOD_ID)) {
                if (remaps.get(map.key.getResourcePath()) != null)
                    map.remap(remaps.get(map.key.getResourcePath()));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerRenders(ModelRegistryEvent event) {
/*
        DissolutionModelLoader.addModel(BlockCasing.PLUG, ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
		DissolutionModelLoader.addModel(BlockCasing.PLUG_CHEST, ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
		DissolutionModelLoader.addModel(BlockCasing.PLUG_HOPPER, ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
    	DissolutionModelLoader.addAllModels(BlockCasing.CASING_BOTTOM, BlockCasing.CASING_TOP, CableBakedModel.INTERSECTION,
				DistillatePipeBakedModel.INTERSECTION);
		DissolutionModelLoader.addModel(CableBakedModel.START, ModelRotation.X0_Y90,
				ModelRotation.X0_Y180, ModelRotation.X0_Y270, ModelRotation.X90_Y0, ModelRotation.X270_Y0);
		DissolutionModelLoader.addModel(DistillatePipeBakedModel.START, ModelRotation.X0_Y90,
				ModelRotation.X0_Y180, ModelRotation.X0_Y270, ModelRotation.X90_Y0, ModelRotation.X270_Y0);
		DissolutionModelLoader.addModel(CableBakedModel.SECTION, ModelRotation.X90_Y0, ModelRotation.X0_Y90);
		DissolutionModelLoader.addModel(DistillatePipeBakedModel.SECTION, ModelRotation.X90_Y0, ModelRotation.X0_Y90);
    	registerSmartRender(POWER_CABLE, CableBakedModel.BAKED_MODEL);
    	registerSmartRender(DISTILLATE_PIPE, DistillatePipeBakedModel.BAKED_MODEL);
    	registerSmartRender(CASING, ModularMachineBakedModel.BAKED_MODEL);
*/
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

    private ModBlocks() {
    }
}
