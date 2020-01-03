/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.common.sound;

import ladysnake.requiem.Requiem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RequiemSoundEvents {
    public static final SoundEvent EFFECT_BECOME_MORTAL = register("effect.become.mortal");
    public static final SoundEvent EFFECT_BECOME_REMNANT = register("effect.become.remnant");
    public static final SoundEvent EFFECT_POSSESSION_ATTEMPT = register("effect.possession.attempt");
    public static final SoundEvent EFFECT_TIME_STOP = register("effect.time.stop");
    public static final SoundEvent ITEM_OPUS_USE = register("item.opus.use");

    private static SoundEvent register(String name) {
        Identifier id = Requiem.id(name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    public static void init() {
        // NO-OP
    }
}
