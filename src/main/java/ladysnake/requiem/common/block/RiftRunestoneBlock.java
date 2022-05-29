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

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.block.ObeliskRune;
import ladysnake.requiem.api.v1.block.VagrantTargetableBlock;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.advancement.RequiemStats;
import ladysnake.requiem.common.entity.effect.AttritionStatusEffect;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.screen.RiftScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Collectors;

public class RiftRunestoneBlock extends InertRunestoneBlock implements ObeliskRune, VagrantTargetableBlock {
    public static final Identifier RIFT_ICON_ID = Requiem.id("textures/gui/rift_icon.png");

    public RiftRunestoneBlock(Settings settings) {
        super(settings);
    }

    public static Optional<Vec3d> findRespawnPosition(EntityType<?> type, World world, BlockPos targetObelisk) {
        return RespawnAnchorBlock.findRespawnPosition(type, world, targetObelisk);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!state.get(ACTIVATED) || !RemnantComponent.isIncorporeal(player) || !this.canBeUsedByVagrant(player)) {
            return ActionResult.PASS;
        } else if (!(world instanceof ServerWorld sw)) {
            return ActionResult.SUCCESS;
        } else {
            RunestoneBlockEntity.findObeliskOrigin(world, pos)
                .filter(origin -> RunestoneBlockEntity.checkForPower(sw, pos))
                .ifPresent(origin -> {
                    player.openHandledScreen(state.createScreenHandlerFactory(world, origin));
                    player.incrementStat(RequiemStats.INTERACT_WITH_RIFT);
                }
            );

            return ActionResult.CONSUME;
        }
    }

    @Override
    public @Nullable NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof RunestoneBlockEntity controller) {
            return new RiftScreenHandlerFactory(
                controller.getDescriptor().orElseThrow(),
                GlobalRecordKeeper.get(world).getRecords().stream()
                    .filter(r -> r.get(RequiemRecordTypes.RIFT_OBELISK).isPresent())
                    .flatMap(r -> r.get(RequiemRecordTypes.OBELISK_REF).stream())
                    .filter(p -> p.dimension() == world.getRegistryKey())
                    .collect(Collectors.toSet()),
                controller::canBeUsedBy);
        }
        return null;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        BlockEntity be = super.createBlockEntity(pos, state);
        return be == null ? new InertRunestoneBlockEntity(pos, state) : be;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public void applyEffect(ServerPlayerEntity target, int runeLevel, int obeliskWidth) {
        // nothing actually
    }

    @Override
    public Identifier getTargetedIcon() {
        return RIFT_ICON_ID;
    }

    @Override
    public boolean canBeUsedByVagrant(PlayerEntity player) {
        StatusEffectInstance statusEffect = player.getStatusEffect(RequiemStatusEffects.ATTRITION);
        return statusEffect == null || statusEffect.getAmplifier() < AttritionStatusEffect.MAX_LEVEL;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InertRunestoneBlockEntity runestone) {
                runestone.setCustomName(itemStack.getName());
            }
        }
    }
}
