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
package ladysnake.requiem.core.possession;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import ladysnake.requiem.api.v1.event.requiem.SoulboundStackCheckCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessedData;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.core.util.OrderedInventory;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public abstract class PossessedDataBase implements PossessedData, AutoSyncedComponent {
    public static void onMobConverted(LivingEntity original, LivingEntity converted) {
        PlayerEntity possessor = ((Possessable) original).getPossessor();
        PossessedData possessedData = KEY.get(converted);
        if (possessor != null) {
            PossessionComponent.get(possessor).stopPossessing(false);
            possessedData.setConvertedUnderPossession();
            // The possession will start when the entity is added to the world
            ((Possessable) converted).setPossessor(possessor);
        }
        // copy possessed data to avoid losing the inventory
        possessedData.copyFrom(KEY.get(original));
    }

    protected final Entity holder;
    private @Nullable NbtCompound hungerData;
    private @Nullable OrderedInventory inventory;
    private boolean convertedUnderPossession;
    private int selectedSlot;

    public PossessedDataBase(Entity holder) {
        this.holder = holder;
    }

    @Override
    public void setConvertedUnderPossession() {
        this.convertedUnderPossession = true;
        // Explicit sync not necessary as the entity will be spawned later
    }

    @Override
    public boolean wasConvertedUnderPossession() {
        return this.convertedUnderPossession;
    }

    @Override
    public void moveItems(PlayerInventory inventory, boolean fromPlayerToThis) {
        if (inventory.player.world.isClient) return;

        if (fromPlayerToThis) {
            this.dropItems();
            this.inventory = new OrderedInventory(inventory.size());
            for (int i = 0; i < inventory.size(); i++) {
                if (!SoulboundStackCheckCallback.EVENT.invoker().isSoulbound(inventory, inventory.getStack(i), i)) {
                    this.inventory.setStack(i, inventory.removeStack(i));
                }
            }
            this.selectedSlot = inventory.selectedSlot;
            this.onPossessed();
        } else {
            if (this.inventory != null) {
                for (int i = 0; i < this.inventory.size(); i++) {
                    this.holder.dropStack(inventory.removeStack(i));
                    inventory.setStack(i, this.inventory.removeStack(i));
                }
                this.inventory = null;
                inventory.selectedSlot = this.selectedSlot;
                ((ServerPlayerEntity) inventory.player).networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(inventory.selectedSlot));
            }
        }
    }

    protected abstract void onPossessed();

    @Override
    public void dropItems() {
        if (this.inventory != null) {
            this.inventory.clearToList().forEach(this.holder::dropStack);
        }
    }

    @Override
    public NbtCompound getHungerData() {
        if (this.hungerData == null) {
            this.hungerData = new NbtCompound();
            this.hungerData.putInt("foodLevel", 20);
        }
        return this.hungerData;
    }

    @Override
    public void copyFrom(PossessedData original) {
        this.readFromNbt(Util.make(new NbtCompound(), original::writeToNbt));
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(this.convertedUnderPossession);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.convertedUnderPossession = buf.readBoolean();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("hunger_data", NbtType.COMPOUND)) {
            this.hungerData = tag.getCompound("hunger_data");
        }

        if (tag.contains("inventory_size", NbtType.NUMBER)) {
            NbtList items = tag.getList("inventory", NbtType.COMPOUND);
            this.inventory = new OrderedInventory(tag.getInt("inventory_size"));
            this.inventory.readNbtList(items);
        }

        this.selectedSlot = tag.getInt("selected_slot");

        if (tag.contains("converted_under_possession")) {
            this.convertedUnderPossession = tag.getBoolean("converted_under_possession");
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (this.hungerData != null) {
            PlayerEntity possessor = ((Possessable) this.holder).getPossessor();
            if (possessor != null) possessor.getHungerManager().writeNbt(this.hungerData);
            tag.put("hunger_data", this.hungerData.copy());
        }
        if (this.inventory != null) {
            tag.putInt("inventory_size", this.inventory.size());
            tag.put("inventory", this.inventory.toNbtList());
        }

        tag.putInt("selected_slot", this.selectedSlot);

        if (this.convertedUnderPossession) {
            tag.putBoolean("converted_under_possession", true);
        }
    }
}
