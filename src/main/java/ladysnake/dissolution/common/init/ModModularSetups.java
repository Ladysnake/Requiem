package ladysnake.dissolution.common.init;

import ladysnake.dissolution.api.ModularMachineSetup;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.SetupOreSieve;
import ladysnake.dissolution.common.blocks.alchemysystem.SetupPowerGenerator;
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
		System.out.println("registering custom setups");
		event.getRegistry().registerAll(new SetupOreSieve(), new SetupPowerGenerator());
	}
	
	private ModModularSetups() {}

}
