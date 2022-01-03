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
package ladysnake.requiem.common.block;

import ladysnake.requiem.api.v1.block.ObeliskEffectRune;
import ladysnake.requiem.common.entity.SoulEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.Optional;
import java.util.function.Supplier;

public class RunestoneBlock extends InertRunestoneBlock implements ObeliskEffectRune {
    public static Optional<Block> getByEffect(StatusEffect effect) {
        Identifier id = Registry.STATUS_EFFECT.getId(effect);
        return Optional.ofNullable(id).flatMap(i ->
            Registry.BLOCK.getOrEmpty(new Identifier(i.getNamespace(), "tachylite/runic/" + i.getPath())));
    }

    private final Supplier<StatusEffect> effect;
    private final int maxLevel;

    public RunestoneBlock(Settings settings, Supplier<StatusEffect> effect, int maxLevel) {
        super(settings);
        this.effect = effect;
        this.maxLevel = maxLevel;
    }

    @Override
    public StatusEffect getEffect() {
        return effect.get();
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext ctx && ctx.getEntity() instanceof SoulEntity) {
            return VoxelShapes.empty();
        }
        return super.getCollisionShape(state, world, pos, context);
    }
}
