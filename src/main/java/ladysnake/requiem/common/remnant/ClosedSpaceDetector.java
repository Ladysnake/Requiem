/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
 */
package ladysnake.requiem.common.remnant;

import ladysnake.requiem.Requiem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.Direction;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Runs a floodfill algorithm to check if a player is in a closed space
 */
public class ClosedSpaceDetector {
    public static final int MAX_RUN_TICKS = 100;
    public static final int REST_TICKS = 400;
    public static final int MAX_BLOCKS_SCANNED_PER_TICK = 100;
    public static final Direction[] DIRECTIONS = Direction.values();

    /**The player for which this detector is running*/
    private final PlayerEntity player;
    /**All blocks that have already been scanned*/
    private Set<BlockPos> scannedBlocks = new HashSet<>();
    /**
     * All blocks that should be scanned
     */
    private Deque<BlockPos> toScan = new ArrayDeque<>();
    private int counter;
    // We do initialize it
    @SuppressWarnings("NullableProblems")
    private DimensionType scanDimension;
    private boolean scanning;

    public ClosedSpaceDetector(PlayerEntity player) {
        this.player = player;
        this.reset(false);
    }

    public void reset(boolean scanning) {
        this.scanning = scanning;
        this.counter = scanning ? MAX_RUN_TICKS : REST_TICKS;
        if (scanning) {
            this.scannedBlocks.clear();
            this.toScan.clear();
            this.toScan.add(this.player.getBlockPos());
            this.scanDimension = this.player.dimension;
        }
    }

    public void tick() {
        counter--;
        if (counter < 0) {
            this.reset(!this.scanning);
        }
        if (!this.scanning) {
            return;
        }
        if (this.scanDimension != this.player.dimension) {
            this.reset(false);
        }
        for (int i = 0; i < MAX_BLOCKS_SCANNED_PER_TICK; i++) {
            if (toScan.isEmpty()) {
                this.onClosedSpaceFound();
                break;
            }
            BlockPos next = toScan.pop();
            // If the scannedBlocks set did not already contain the next pos, proceed
            if (this.player.world.getBlockState(next).getCollisionShape(this.player.world, next).isEmpty()) {
                this.scannedBlocks.add(next);
                for (Direction direction : DIRECTIONS) {
                    BlockPos neigh = next.offset(direction);
                    if (!this.scannedBlocks.contains(neigh) && !toScan.contains(neigh)) {
                        toScan.add(neigh);
                    }
                }
            }
        }
    }

    private void onClosedSpaceFound() {
        // The player has left the closed space somehow
        if (!this.scannedBlocks.contains(player.getBlockPos())) {
            return;
        }
        Requiem.LOGGER.info("CLOSED SPACE");
        EndermanEntity bart = this.player.world.getClosestEntity(EndermanEntity.class, TargetPredicate.DEFAULT, player, player.x, player.y, player.z, new BoundingBox(player.getBlockPos()).expand(20));
        if (bart == null) {
            bart = new EndermanEntity(EntityType.ENDERMAN, this.player.world);
            bart.copyPositionAndRotation(this.player);
            bart.setCustomName(new TextComponent("Bart"));
            this.player.world.spawnEntity(bart);
        }
        bart.teleport(player.x, player.y, player.z, true);
        this.reset(false);
    }
}
