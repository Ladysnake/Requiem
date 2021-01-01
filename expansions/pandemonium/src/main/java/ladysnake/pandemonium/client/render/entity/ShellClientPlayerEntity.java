/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.pandemonium.client.render.entity;

import com.mojang.authlib.GameProfile;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class ShellClientPlayerEntity extends OtherClientPlayerEntity {
    private final PlayerShellEntity shell;
    private final PlayerListEntry playerListEntry;

    public ShellClientPlayerEntity(PlayerShellEntity shell, GameProfile profile) {
        super((ClientWorld) shell.world, profile);
        this.shell = shell;
        this.playerListEntry = new PlayerListEntry(new PlayerListS2CPacket().new Entry(profile, 0, GameMode.SURVIVAL, shell.getDisplayName()));
        this.copyPositionAndRotation(shell);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return this.shell.getEquippedStack(slot);
    }

    @Override
    public boolean isPartVisible(PlayerModelPart modelPart) {
        return this.shell.isPartVisible(modelPart);
    }

    @Override
    public CompoundTag getShoulderEntityLeft() {
        return this.shell.getShoulderEntityLeft();
    }

    @Override
    public CompoundTag getShoulderEntityRight() {
        return this.shell.getShoulderEntityRight();
    }

    @Override
    public boolean isOnFire() {
        return this.shell.isOnFire();
    }

    @Override
    public boolean hasVehicle() {
        return this.shell.hasVehicle();
    }

    @Override
    public @Nullable Entity getVehicle() {
        return this.shell.getVehicle();
    }

    public void updateData() {
        this.copyPositionAndRotation(this.shell);
        this.hurtTime = this.shell.hurtTime;
        this.deathTime = this.shell.deathTime;
    }

    @Nullable
    @Override
    protected PlayerListEntry getPlayerListEntry() {
        return this.playerListEntry;
    }
}
