/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.sound;

import ladysnake.requiem.Requiem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class RequiemSoundEvents {
    public static final SoundEvent BLOCK_OBELISK_ACTIVATE = register("block.obelisk.activate");
    public static final SoundEvent BLOCK_OBELISK_AMBIENT = register("block.obelisk.ambient");
    public static final SoundEvent BLOCK_OBELISK_CHARGE = register("block.obelisk.charge");
    public static final SoundEvent BLOCK_OBELISK_DEACTIVATE = register("block.obelisk.deactivate");
    public static final SoundEvent BLOCK_RUNESTONE_CARVE = register("block.runestone.carve");
    public static final SoundEvent BLOCK_RUNESTONE_CLEAR = register("block.runestone.clear");
    public static final SoundEvent EFFECT_BECOME_MORTAL = register("effect.become.mortal");
    public static final SoundEvent EFFECT_BECOME_REMNANT = register("effect.become.remnant");
    public static final SoundEvent EFFECT_DISSOCIATE = register("effect.dissociate");
    public static final SoundEvent EFFECT_PHASE = register("effect.phase");
    public static final SoundEvent EFFECT_POSSESSION_ATTEMPT = register("effect.possession.attempt");
    public static final SoundEvent EFFECT_RECLAMATION_CLEAR = register("effect.reclamation.clear");
    public static final SoundEvent EFFECT_TIME_STOP = register("effect.time.stop");
    public static final SoundEvent ENTITY_MORTICIAN_AMBIENT = register("entity.mortician.ambient");
    public static final SoundEvent ENTITY_MORTICIAN_CAST_SPELL = register("entity.mortician.cast_spell");
    public static final SoundEvent ENTITY_MORTICIAN_PREPARE_ATTACK = register("entity.mortician.prepare_attack");
    public static final SoundEvent ENTITY_MORTICIAN_DEATH = register("entity.mortician.death");
    public static final SoundEvent ENTITY_MORTICIAN_NO = register("entity.mortician.no");
    public static final SoundEvent ENTITY_MORTICIAN_TRADE = register("entity.mortician.trade");
    public static final SoundEvent ENTITY_MORTICIAN_YES = register("entity.mortician.yes");
    public static final SoundEvent ENTITY_MORTICIAN_HURT = register("entity.mortician.hurt");
    public static final SoundEvent ENTITY_SOUL_TELEPORT = register("entity.soul.teleport");
    public static final SoundEvent ENTITY_SOUL_DISINTEGRATES = register("entity.soul.disintegrate");
    public static final SoundEvent ENTITY_OBELISK_SOUL_DISINTEGRATES = register("entity.obelisk_soul.disintegrate");
    public static final SoundEvent ITEM_OPUS_USE = register("item.opus.use");
    public static final SoundEvent ITEM_EMPTY_VESSEL_USE = register("item.empty_vessel.use");
    public static final SoundEvent ITEM_FILLED_VESSEL_USE = register("item.filled_vessel.use");

    private static SoundEvent register(String name) {
        Identifier id = Requiem.id(name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    public static void init() {
        // NO-OP
    }
}
