package ladysnake.dissolution.common.blocks.alchemysystem;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.api.ModularMachineSetup;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor.IMachine.PowerConsumption;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.util.ResourceLocation;

public class SetupPowerGenerator extends ModularMachineSetup {
	
	public static final ImmutableSet<ItemAlchemyModule> setup = ImmutableSet.of(
			ItemAlchemyModule.getFromType(AlchemyModule.GENERATOR, 1),
			ItemAlchemyModule.getFromType(AlchemyModule.INTERFACE, 1));
	
	public SetupPowerGenerator() {
		this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "power_generator"));
	}

	@Override
	public void onTick(TileEntityModularMachine te) {
		te.setPowerConsumption(PowerConsumption.GENERATOR);
	}

	@Override
	public ImmutableSet<ItemAlchemyModule> getSetup() {
		return setup;
	}

}
