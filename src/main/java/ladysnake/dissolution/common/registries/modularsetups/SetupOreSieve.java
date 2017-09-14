package ladysnake.dissolution.common.registries.modularsetups;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.AlchemyModule;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.util.ResourceLocation;

public class SetupOreSieve extends ModularMachineSetup {
	
	private static final ImmutableSet<ItemAlchemyModule> setup = ImmutableSet.of(
			ItemAlchemyModule.getFromType(AlchemyModule.CONTAINER, 1), 
			ItemAlchemyModule.getFromType(AlchemyModule.INTERFACE, 1), 
			ItemAlchemyModule.getFromType(AlchemyModule.FILTER, 1));
	
	public SetupOreSieve() {
		this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "ore_sieve"));
	}
	
	@Override
	public ImmutableSet<ItemAlchemyModule> getSetup() {
		return setup;
	}

	@Override
	public void onTick(TileEntityModularMachine te) {
		if(te.isPowered() && !te.getWorld().isRemote)
			System.out.println("SIEVE");
	}

	@Override
	public void init(TileEntityModularMachine te) {
		// TODO Auto-generated method stub
		
	}

}
