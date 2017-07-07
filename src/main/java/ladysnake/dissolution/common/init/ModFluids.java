package ladysnake.dissolution.common.init;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.BlockFluidMercury;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum ModFluids {
	
	MERCURY("mercury", false,
			fluid -> fluid.setLuminosity(5).setDensity(1600).setViscosity(1000),
			BlockFluidMercury::new);
	// just add new fluids here

	/**The forge fluid associated with this block*/
	public final Fluid fluid;
	/**The fluid block*/
	public final BlockFluidBase fluidBlock;
	
	/**The register manager*/
	static final RegisterManager REGISTRY_MANAGER = new RegisterManager();
	
	/**The path to the fluid blockstate*/
	private static final String FLUID_MODEL_PATH = Reference.MOD_ID + ":" + "fluid";
	
	/**
	 * Creates a fluid
	 * @param name
	 * @param hasFlowIcon if set to false, the fluid will use the same texture for flowing and still
	 * @param fluidPropertyApplier a Consumer that applies various properties to the forge fluid
	 * @param blockFactory the constructor of the fluid block
	 */
	ModFluids(final String name, final boolean hasFlowIcon, final Consumer<Fluid> fluidPropertyApplier, final Function<Fluid, BlockFluidBase> blockFactory) {
		final String texturePrefix = Reference.MOD_ID + ":" + "blocks/fluid_";

		final ResourceLocation still = new ResourceLocation(texturePrefix + name + "_still");
		final ResourceLocation flowing = hasFlowIcon ? new ResourceLocation(texturePrefix + name + "_flow") : still;

		Fluid fluid = new Fluid(name, still, flowing);
		final boolean useOwnFluid = FluidRegistry.registerFluid(fluid);

		if (useOwnFluid) {
			fluidPropertyApplier.accept(fluid);
		} else {
			fluid = FluidRegistry.getFluid(name);
		}
		
		this.fluidBlock = blockFactory.apply(fluid);
		this.fluid = fluid;
	}
	
	private void registerFluidBlock() {
		fluidBlock.setRegistryName(Reference.MOD_ID, "fluid." + fluid.getName());
		fluidBlock.setUnlocalizedName(Reference.MOD_ID + ":" + fluid.getUnlocalizedName());
		ModBlocks.INSTANCE.registerBlock(fluidBlock, false);
	}

	private void registerFluidModel() {
		final Item item = Item.getItemFromBlock((Block) fluidBlock);
		if(item == Items.AIR) {
			LogManager.getLogger().warn("(Dissolution) + " + fluidBlock.getRegistryName() + " : the passed in fluid block has no associated item");
			return;
		}

		ModelBakery.registerItemVariants(item);

		final ModelResourceLocation modelResourceLocation = new ModelResourceLocation(FLUID_MODEL_PATH, fluid.getName());
		
		ModelLoader.setCustomMeshDefinition(item, stack -> modelResourceLocation);

		ModelLoader.setCustomStateMapper((Block) fluidBlock, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(final IBlockState state) {
				return modelResourceLocation;
			}
		});
	}
	
	public static final class RegisterManager {
		
		@SubscribeEvent
	    public void onRegister(RegistryEvent.Register<Block> event) {
	    	for (final ModFluids modFluid : ModFluids.values())
	    		modFluid.registerFluidBlock();
	    	registerFluidContainers();
		}
		
		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public void registerAllModels(final ModelRegistryEvent event) {
			for(ModFluids mf : ModFluids.values())
				mf.registerFluidModel();
		}

		private void registerFluidContainers() {
			//FluidRegistry.addBucketForFluid(MERCURY); //Actually we don't because you don't put a magic liquid in a bucket m8
		}
		
		private RegisterManager() {}
	}
	/*
	private static interface MeshDefinitionFix extends ItemMeshDefinition {
		ModelResourceLocation getLocation(final ItemStack stack);

		static ItemMeshDefinition create(final MeshDefinitionFix lambda) {
			return lambda;
		}

		@Override
		default ModelResourceLocation getModelLocation(final ItemStack stack) {
			return getLocation(stack);
		}
	}*/



}
