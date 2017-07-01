package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public enum ModSounds {
	lost_soul_ambient_1,
	lost_soul_ambient_2,
	lost_soul_pain,
	lost_soul_death;
		
	public final SoundEvent sound;
		
	ModSounds() {
		this.sound = new SoundEvent(new ResourceLocation(Reference.MOD_ID, this.name()));
	}

	@SubscribeEvent
    public void onRegister(RegistryEvent.Register<SoundEvent> event) {
		IForgeRegistry<SoundEvent> reg = event.getRegistry();
		for(ModSounds s : ModSounds.values())
			reg.register(s.sound);
	}
}
