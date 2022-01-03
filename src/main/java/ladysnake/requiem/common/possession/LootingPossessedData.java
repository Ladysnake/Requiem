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
package ladysnake.requiem.common.possession;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.loot.RequiemLootTables;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import ladysnake.requiem.core.possession.PossessedDataBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class LootingPossessedData extends PossessedDataBase {
    private boolean previouslyPossessed;

    public LootingPossessedData(Entity holder) {
        super(holder);
    }

    @Override
    public void giftFirstPossessionLoot(PlayerEntity player) {
        if (!this.previouslyPossessed) {
            this.dropLoot(player);
            this.previouslyPossessed = true;
        }
    }

    @Override
    public void clientTick() {
        if (this.wasConvertedUnderPossession()) {
            Entity camera = MinecraftClient.getInstance().getCameraEntity();
            World world = holder.world;
            if (camera != null && RemnantComponent.isIncorporeal(camera)) {
                if (world.random.nextFloat() > 0.9f) {
                    for (int i = 0; i < world.random.nextInt(3); i++) {
                        double vx = world.random.nextGaussian() * 0.03D;
                        double vy = world.random.nextGaussian() * 0.03D;
                        double vz = world.random.nextGaussian() * 0.03D;
                        world.addParticle(RequiemParticleTypes.ATTUNED, holder.getParticleX(0.5D), holder.getRandomBodyY(), holder.getParticleZ(0.5D), vx, vy, vz);
                    }
                }
            }
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
        lootTable.generateLoot(builder.build(RequiemLootTables.POSSESSION), player.getInventory()::offerOrDrop);
    }

    private Identifier getLootTable() {
        Identifier identifier = Registry.ENTITY_TYPE.getId(this.holder.getType());
        return new Identifier(identifier.getNamespace(), "requiem/possession/" + identifier.getPath());
    }

    @Override
    protected void onPossessed() {
        this.previouslyPossessed = true;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        super.readFromNbt(tag);

        if (tag.contains("previously_possessed")) {
            this.previouslyPossessed = tag.getBoolean("previously_possessed");
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        super.writeToNbt(tag);

        if (this.previouslyPossessed) {
            tag.putBoolean("previously_possessed", true);
        }
    }
}
