package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public enum ModSounds {
    wisp_ambiance_1, wisp_ambiance_2, wisp_ambiance_3;

    private final SoundEvent sound;

    ModSounds() {
        ResourceLocation soundLocation = new ResourceLocation(Reference.MOD_ID, this.toString());
        this.sound = new SoundEvent(soundLocation);
        this.sound.setRegistryName(soundLocation);
    }

    public SoundEvent sound() {
        return this.sound;
    }

    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<SoundEvent> event) {
        IForgeRegistry<SoundEvent> reg = event.getRegistry();
        for (ModSounds s : ModSounds.values())
            reg.register(s.sound);
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
