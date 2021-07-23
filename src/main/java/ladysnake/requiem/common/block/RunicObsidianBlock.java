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
package ladysnake.requiem.common.block;

import ladysnake.requiem.api.v1.block.ObeliskEffectRune;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public class RunicObsidianBlock extends BlockWithEntity implements ObeliskEffectRune {
    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");

    private final Supplier<StatusEffect> effect;
    private final int maxLevel;

    public RunicObsidianBlock(Settings settings, Supplier<StatusEffect> effect, int maxLevel) {
        super(settings);
        this.effect = effect;
        this.maxLevel = maxLevel;
        this.setDefaultState(this.stateManager.getDefaultState().with(ACTIVATED, false));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world instanceof ServerWorld sw) {
            tryActivateObelisk(sw, pos);
        }
        if (player.getStackInHand(hand).getItem() == RequiemItems.DEBUG_ITEM) {
            if (!world.isClient && world.getBlockEntity(pos) instanceof RunicObsidianBlockEntity core) {
                player.sendMessage(new LiteralText("Width: %d, Height: %d".formatted(core.getRangeLevel(), core.getPowerLevel())), true);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        world.getBlockTickScheduler().schedule(pos, this, 0);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        world.getBlockTickScheduler().schedule(pos, this, 0);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        tryActivateObelisk(world, pos);
    }

    public static void tryActivateObelisk(ServerWorld world, BlockPos pos) {
        RunicObsidianBlockEntity.findObeliskOrigin(world, pos).flatMap(origin -> RunicObsidianBlockEntity.matchObelisk(world, origin).result())
            .ifPresentOrElse(
                match -> {
                    if (toggleRune(world, pos, true) && match.origin().equals(pos)) {
                        BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_POWER_SELECT);
                    }
                },
                () -> {
                    if (toggleRune(world, pos, false)) {
                        if (world.getBlockEntity(pos) instanceof RunicObsidianBlockEntity be && be.delegate == null) {
                            BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
                        }
                        BeaconBlockEntity.playSound(world, pos, RequiemSoundEvents.ITEM_FILLED_VESSEL_USE);
                    }
                }
            );
    }

    private static boolean toggleRune(ServerWorld world, BlockPos runePos, boolean activated) {
        BlockState blockState = world.getBlockState(runePos);
        if (blockState.get(ACTIVATED) != activated) {
            world.setBlockState(runePos, blockState.cycle(ACTIVATED), Block.NOTIFY_LISTENERS | Block.NOTIFY_NEIGHBORS);
            return true;
        }
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(ACTIVATED) ? new RunicObsidianBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, RequiemBlockEntities.RUNIC_OBSIDIAN, RunicObsidianBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public StatusEffect getEffect() {
        return effect.get();
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }
}
