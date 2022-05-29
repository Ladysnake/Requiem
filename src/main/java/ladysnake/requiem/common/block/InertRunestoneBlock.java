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

import ladysnake.requiem.api.v1.event.minecraft.BlockReplacedCallback;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.tag.RequiemBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
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

public class InertRunestoneBlock extends BlockWithEntity {
    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");
    public static final BooleanProperty HEAD = BooleanProperty.of("head");

    public InertRunestoneBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(ACTIVATED, false).with(HEAD, false));
    }

    public static void registerCallbacks() {
        BlockReplacedCallback.EVENT.register((oldState, world, pos, newState, moved) -> {
            if (oldState.isIn(RequiemBlockTags.OBELISK_FRAME) != newState.isIn(RequiemBlockTags.OBELISK_FRAME)) {
                for (BlockPos checkedPos : BlockPos.iterate(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
                    // only tachylite blocks in diagonals from core blocks matter
                    if (checkedPos.getManhattanDistance(pos) > 1) {
                        BlockState checkedState = world.getBlockState(checkedPos);
                        if (checkedState.isIn(RequiemBlockTags.OBELISK_CORE)) {
                            world.createAndScheduleBlockTick(checkedPos, checkedState.getBlock(), 0);
                        }
                    }
                }
            }
        });
    }

    public static void tryActivateObelisk(ServerWorld world, BlockPos pos, boolean firstActivation) {
        RunestoneBlockEntity.findObeliskOrigin(world, pos).flatMap(origin -> RunestoneBlockEntity.matchObelisk(world, origin).result())
            .ifPresentOrElse(
                match -> {
                    if (toggleRune(world, pos, match)) {
                        if (firstActivation && match.origin().equals(pos)) {
                            world.playSound(null, pos, RequiemSoundEvents.BLOCK_OBELISK_ACTIVATE, SoundCategory.BLOCKS, 1, 0.7F);
                        }
                        world.playSound(null, pos, RequiemSoundEvents.BLOCK_OBELISK_CHARGE, SoundCategory.BLOCKS, 1, 0.6f);
                    }
                },
                () -> toggleRune(world, pos, null)
            );
    }

    private static boolean toggleRune(ServerWorld world, BlockPos runePos, @Nullable ObeliskMatch match) {
        BlockState blockState = world.getBlockState(runePos);
        if (blockState.getBlock() instanceof InertRunestoneBlock runestone) {
            return runestone.toggleRune(world, runePos, match, blockState);
        }
        return false;
    }

    protected boolean toggleRune(ServerWorld world, BlockPos runePos, @Nullable ObeliskMatch match, BlockState blockState) {
        boolean activated = match != null;
        boolean head = activated && match.origin().equals(runePos);
        if (blockState.get(ACTIVATED) != activated || blockState.get(HEAD) != head) {
            BlockEntity oldBe = world.getBlockEntity(runePos);
            if (oldBe instanceof RunestoneBlockEntity runestoneController) runestoneController.onDestroyed();
            world.removeBlockEntity(runePos);
            world.setBlockState(runePos, blockState.with(ACTIVATED, activated).with(HEAD, head), Block.NOTIFY_LISTENERS | Block.NOTIFY_NEIGHBORS);

            if (oldBe instanceof InertRunestoneBlockEntity runestoneBe && world.getBlockEntity(runePos) instanceof InertRunestoneBlockEntity newRunestoneBe) {
                newRunestoneBe.setCustomName(runestoneBe.getCustomName().orElse(null));
            }

            return true;
        }
        return false;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(hand).getItem() == RequiemItems.DEBUG_ITEM) {
            if (!world.isClient && RunestoneBlockEntity.findObeliskOrigin(world, pos).map(world::getBlockEntity).orElse(null) instanceof RunestoneBlockEntity core) {
                player.sendMessage(new LiteralText("Width: %d, Height: %d".formatted(core.getRangeLevel(), core.getPowerLevel())), true);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isIn(RequiemBlockTags.OBELISK_CORE)) {
            world.createAndScheduleBlockTick(pos, this, 0);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.get(HEAD) && (!(newState.getBlock() instanceof InertRunestoneBlock) || !newState.get(HEAD))) {
            if (world.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone) {
                runestone.onDestroyed();
            }
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        world.createAndScheduleBlockTick(pos, this, 0);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        tryActivateObelisk(world, pos, true);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED).add(HEAD);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(HEAD) ? new RunestoneBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        return checkType(type, RequiemBlockEntities.RUNIC_OBSIDIAN, RunestoneBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
