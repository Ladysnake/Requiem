package ladysnake.dissolution.common.init;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.BlockFluidMercury;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

public class ModFluids {

	public static final Set<Fluid> FLUIDS = new HashSet<>();

	public static final Map<String, IFluidBlock> MOD_FLUID_BLOCKS = new HashMap<>();
	

	public static final Fluid MERCURY = createFluid("mercury", false,
			fluid -> fluid.setLuminosity(10).setDensity(1600).setViscosity(1000),
			BlockFluidMercury::new);

	private static Fluid createFluid(final String name, final boolean hasFlowIcon, final Consumer<Fluid> fluidPropertyApplier, final Function<Fluid, BlockFluidBase> blockFactory) {
		final String texturePrefix = Reference.MOD_ID + ":" + "blocks/fluid_";

		final ResourceLocation still = new ResourceLocation(texturePrefix + name + "_still");
		final ResourceLocation flowing = hasFlowIcon ? new ResourceLocation(texturePrefix + name + "_flow") : still;

		Fluid fluid = new Fluid(name, still, flowing);
		final boolean useOwnFluid = FluidRegistry.registerFluid(fluid);

		if (useOwnFluid) {
			fluidPropertyApplier.accept(fluid);
			MOD_FLUID_BLOCKS.put(name, blockFactory.apply(fluid));
		} else {
			fluid = FluidRegistry.getFluid(name);
		}

		FLUIDS.add(fluid);

		return fluid;
	}

	private static void registerFluidContainers() {
		//FluidRegistry.addBucketForFluid(MERCURY); //Actually we don't because you don't put a magic liquid in a bucket m8
	}


}
