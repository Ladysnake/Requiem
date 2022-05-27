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
package ladysnake.requiem.core.entity;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.record.EntityPositionClerk;
import ladysnake.requiem.core.tag.RequiemCoreTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SoulHolderComponent implements AutoSyncedComponent {
    public static final ComponentKey<SoulHolderComponent> KEY = ComponentRegistry.getOrCreate(RequiemCore.id("soul_holder"), SoulHolderComponent.class);
    public static final Identifier SOUL_CAPTURE_MECHANISM_ID = RequiemCore.id("soul_capture");

    public static boolean isSoulless(LivingEntity target) {
        return target.getType().isIn(RequiemCoreTags.Entity.SOULLESS) || get(target).removedSoul;
    }

    public static void onMobConverted(LivingEntity original, LivingEntity converted) {
        KEY.get(converted).setSoulRemoved(KEY.get(original).removedSoul);
        EntityPositionClerk.get(converted).transferFrom(EntityPositionClerk.get(original));
    }

    private final LivingEntity owner;
    private boolean removedSoul;

    public SoulHolderComponent(LivingEntity owner) {
        this.owner = owner;
    }

    public static SoulHolderComponent get(LivingEntity target) {
        return KEY.get(target);
    }

    public void removeSoul() {
        this.setSoulRemoved(true);
    }

    public void giveSoulBack() {
        this.setSoulRemoved(false);
    }

    private void setSoulRemoved(boolean removed) {
        if (this.removedSoul != removed) {
            this.removedSoul = removed;
            EntityAiToggle.get(this.owner).toggleAi(SoulHolderComponent.SOUL_CAPTURE_MECHANISM_ID, !this.owner.getType().isIn(RequiemCoreTags.Entity.SOULLESS) && removed, false);
            KEY.sync(this.owner);
        }
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(this.removedSoul);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.removedSoul = buf.readBoolean();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.setSoulRemoved(tag.getBoolean("removed_soul"));
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("removed_soul", removedSoul);
    }
}
