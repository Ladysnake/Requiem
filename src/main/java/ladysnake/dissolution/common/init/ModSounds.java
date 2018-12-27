package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Ref;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = Ref.MOD_ID)
public enum ModSounds {
    ;

    private final SoundEvent sound;

    ModSounds() {
        ResourceLocation soundLocation = new ResourceLocation(Ref.MOD_ID, this.toString());
        this.sound = new SoundEvent(soundLocation);
        this.sound.setRegistryName(soundLocation);
    }

    public SoundEvent sound() {
        return this.sound;
    }

    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<SoundEvent> event) {
        IForgeRegistry<SoundEvent> reg = event.getRegistry();
        for (ModSounds s : ModSounds.values()) {
            reg.register(s.sound);
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
