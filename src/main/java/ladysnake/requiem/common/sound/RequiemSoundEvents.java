package ladysnake.requiem.common.sound;

import ladysnake.requiem.Requiem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

public class RequiemSoundEvents {
    public static final SoundEvent EFFECT_BECOME_MORTAL = register("effect.become.mortal");
    public static final SoundEvent EFFECT_BECOME_REMNANT = register("effect.become.remnant");
    public static final SoundEvent EFFECT_POSSESSION_ATTEMPT = register("effect.possession.attempt");
    public static final SoundEvent EFFECT_TIME_STOP = register("effect.time.stop");
    public static final SoundEvent ITEM_OPUS_USE = register("item.opus.use");

    private static SoundEvent register(String name) {
        return Registry.register(Registry.SOUND_EVENT, name, new SoundEvent(Requiem.id(name)));
    }
}
