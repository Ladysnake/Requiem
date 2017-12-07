package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.*;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.items.InventoryItemBlock;
import ladysnake.dissolution.common.items.ItemSoulInAJar;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
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
import java.util.function.Function;

@SuppressWarnings("WeakerAccess")
public final class ModBlocks {

    /**
     * Used to register stuff
     */
    static final ModBlocks INSTANCE = new ModBlocks();

    public static Block CINNABAR;
    public static Block CINNABAR_ORE;
    public static Block VERMILLION_CONCRETE, VERMILLION_CONCRETE_POWDER,
            VERMILLION_GLASS, VERMILLION_GLAZED_TERRACOTTA, VERMILLION_TERRACOTTA,
            VERMILLION_WOOL;
    public static Block DOLOSTONE;
    public static Block IGNEOUS_ROCK;
    public static Block HALITE;
    public static Block SULPHUR;
    public static Block MAGNET;
    public static BlockVermillionBed VERMILLION_BED;
    public static BlockCrucible CRUCIBLE;
    public static BlockCasing CASING;
    public static BlockDepleted DEPLETED_CLAY;
    public static BlockDepleted DEPLETED_COAL;
    public static BlockDepletedMagma DEPLETED_MAGMA;
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
                CINNABAR = name(new Block(Material.ROCK).setHardness(1.5F).setResistance(10.0F), "cinnabar_block"),
                CINNABAR_ORE = name(new BlockCinnabarOre().setHardness(1.5F).setResistance(5.0F), "cinnabar_ore"),
                VERMILLION_CONCRETE = name(new Block(Material.ROCK, MapColor.ADOBE).setHardness(1.8F), "vermillion_concrete"),
                VERMILLION_CONCRETE_POWDER = name(new Block(Material.ROCK, MapColor.ADOBE).setHardness(0.5F), "vermillion_concrete_powder"),
                VERMILLION_GLASS = name(new BlockGlass(Material.GLASS, false).setHardness(0.3F), "vermillion_glass"),
                VERMILLION_GLAZED_TERRACOTTA = name(new Block(Material.ROCK, MapColor.ORANGE_STAINED_HARDENED_CLAY), "vermillion_glazed_terracotta"),
                VERMILLION_TERRACOTTA = name(new Block(Material.ROCK, MapColor.ORANGE_STAINED_HARDENED_CLAY), "vermillion_terracotta"),
                VERMILLION_WOOL = name(new Block(Material.CLOTH, MapColor.ADOBE), "vermillion_wool"),
                DOLOSTONE = name(new Block(Material.ROCK).setHardness(1.0F), "dolostone"),
                IGNEOUS_ROCK = name(new Block(Material.ROCK).setHardness(1.5F).setResistance(10.0F), "igneous_rock_block"),
                HALITE = name(new Block(Material.ROCK).setHardness(1.5F).setResistance(10.0F), "halite_block"),
                SULPHUR = name(new Block(Material.ROCK).setHardness(1.5F).setResistance(10.0F), "sulfur_block"),
                LAMENT_STONE = name(new BlockLamentStone(), "lament_stone"),
                DEPLETED_CLAY = name(new BlockDepletedClay(), "depleted_clay_block"),
                DEPLETED_COAL = name(new BlockDepleted(Material.ROCK), "depleted_coal_block"),
                DEPLETED_MAGMA = name(new BlockDepletedMagma(), "depleted_magma"),
                SHRINE = name(new BlockShrine(), "passeress_shrine"));
        registerBlock(blockRegistry, MORTAR = name(new BlockMortar(), "mortar"), true,
                block -> new InventoryItemBlock(block, true, false, false));
        registerBlock(blockRegistry, CRUCIBLE = name(new BlockCrucible(), "crucible"), true,
                block -> new InventoryItemBlock(block, true, true, true));
        ModItems.SOUL_IN_A_FLASK = registerBlock(blockRegistry, WISP_IN_A_JAR = name(new BlockWisp(), "wisp_in_a_jar"), true,
                ItemSoulInAJar::new);
        blockRegistry.register(SEPULTURE = name(new BlockSepulture(), "stone_burial"));
        blockRegistry.register(VERMILLION_BED = (BlockVermillionBed) name(new BlockVermillionBed(), "vermillion_bed").setHardness(0.2F));
    }

    private void registerBlocks(IForgeRegistry<Block> blockRegistry, Block... blocks) {
        for (Block b : blocks)
            registerBlock(blockRegistry, b);
    }

    private void registerBlock(IForgeRegistry<Block> blockRegistry, Block block) {
        registerBlock(blockRegistry, block, true, ItemBlock::new);
    }

    @SuppressWarnings("unchecked")
    <T extends ItemBlock> T registerBlock(IForgeRegistry<Block> blockRegistry, Block block, boolean addToTab, Function<Block, T> blockItemFunction) {
        blockRegistry.register(block);
        assert block.getRegistryName() != null;
        T item = (T) blockItemFunction.apply(block).setRegistryName(block.getRegistryName());
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
