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
package ladysnake.requiem.common.impl.possession;

import ladysnake.requiem.api.v1.event.requiem.SoulboundStackCheckCallback;
import ladysnake.requiem.api.v1.possession.PossessedData;
import ladysnake.requiem.common.loot.RequiemLootTables;
import ladysnake.requiem.common.util.OrderedInventory;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class PossessedDataImpl implements PossessedData {
    private final Entity holder;
    private @Nullable CompoundTag hungerData;
    private @Nullable OrderedInventory inventory;
    private boolean previouslyPossessed;

    public PossessedDataImpl(Entity holder) {
        this.holder = holder;
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
            this.previouslyPossessed = true;
        } else {
            if (this.inventory != null) {
                for (int i = 0; i < this.inventory.size(); i++) {
                    this.holder.dropStack(inventory.removeStack(i));
                    inventory.setStack(i, this.inventory.removeStack(i));
                }
                this.inventory = null;
            }
        }
    }

    @Override
    public void giftFirstPossessionLoot(PlayerEntity player) {
        if (!this.previouslyPossessed) {
            this.dropLoot(player);
            this.previouslyPossessed = true;
        }
    }

    protected void dropLoot(PlayerEntity player) {
        Identifier identifier = this.getLootTable();
        ServerWorld world = (ServerWorld) this.holder.world;
        LootTable lootTable = world.getServer().getLootManager().getTable(identifier);
        LootContext.Builder builder = new LootContext.Builder(world)
            .random(world.random)
            .parameter(LootContextParameters.THIS_ENTITY, this.holder)
            .parameter(LootContextParameters.ORIGIN, this.holder.getPos())
            .luck(player.getLuck());
        lootTable.generateLoot(builder.build(RequiemLootTables.POSSESSION), s -> player.inventory.offerOrDrop(world, s));
    }

    private Identifier getLootTable() {
        Identifier identifier = Registry.ENTITY_TYPE.getId(this.holder.getType());
        return new Identifier(identifier.getNamespace(), "requiem/possession/" + identifier.getPath());
    }

    @Override
    public void dropItems() {
        if (this.inventory != null) {
            this.inventory.clearToList().forEach(this.holder::dropStack);
        }
    }

    @Override
    public CompoundTag getHungerData() {
        if (this.hungerData == null) {
            this.hungerData = new CompoundTag();
            this.hungerData.putInt("foodLevel", 20);
        }
        return this.hungerData;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("hunger_data", NbtType.COMPOUND)) {
            this.hungerData = tag.getCompound("hunger_data");
        }

        if (tag.contains("inventory_size", NbtType.NUMBER)) {
            ListTag items = tag.getList("inventory", NbtType.COMPOUND);
            this.inventory = new OrderedInventory(tag.getInt("inventory_size"));
            this.inventory.readTags(items);
        }

        if (tag.contains("previously_possessed")) {
            this.previouslyPossessed = tag.getBoolean("previously_possessed");
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        if (this.hungerData != null) tag.put("hunger_data", this.hungerData.copy());
        if (this.inventory != null) {
            tag.putInt("inventory_size", this.inventory.size());
            tag.put("inventory", this.inventory.getTags());
        }

        if (this.previouslyPossessed) {
            tag.putBoolean("previously_possessed", true);
        }
    }
}
