package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.registries.modularsetups.ModularMachineSetup;
import ladysnake.dissolution.common.registries.modularsetups.SetupCrystallizer;
import ladysnake.dissolution.common.registries.modularsetups.SetupOreSieve;
import ladysnake.dissolution.common.registries.modularsetups.SetupPowerGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public final class ModModularSetups {
	
	public static final IForgeRegistry<ModularMachineSetup> REGISTRY = new RegistryBuilder<ModularMachineSetup>().setName(new ResourceLocation(Reference.MOD_ID, "modularmachinesetups")).setType(ModularMachineSetup.class).create();
	static final ModModularSetups INSTANCE = new ModModularSetups();
	
	@SubscribeEvent
	public void onRegister(RegistryEvent.Register<ModularMachineSetup> event) {
		event.getRegistry().registerAll(new SetupOreSieve(), new SetupPowerGenerator(), new SetupCrystallizer());
	}
	
	private ModModularSetups() {}

}
