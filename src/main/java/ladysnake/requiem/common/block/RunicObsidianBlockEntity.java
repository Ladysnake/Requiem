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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ladysnake.requiem.api.v1.block.ObeliskRune;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.tag.RequiemBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class RunicObsidianBlockEntity extends BlockEntity implements Tickable {
    public static final Direction[] OBELISK_SIDES = {Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
    public static final int POWER_ATTEMPTS = 1;
    private final Object2IntMap<StatusEffect> levels = new Object2IntOpenHashMap<>();
    private @Nullable BlockPos delegate;
    private int obeliskWidth = 0;
    private int obeliskHeight = 0;

    public RunicObsidianBlockEntity() {
        super(RequiemBlockEntities.RUNIC_OBSIDIAN);
    }

    @Override
    public void tick() {
        assert this.world != null;
        if (this.world.isClient) return;

        // Salt the time to avoid checking every potential obelisk on the same tick
        if ((this.world.getTime() + this.pos.hashCode()) % 80L == 0L) {
            this.refresh();

            Vec3d obeliskCenter = new Vec3d(
                MathHelper.lerp(0.5, this.pos.getX(), this.pos.getX() + obeliskWidth - 1),
                this.pos.getY() - 2,
                MathHelper.lerp(0.5, this.pos.getZ(), this.pos.getZ() + obeliskWidth - 1)
            );

            if (!this.levels.isEmpty() && this.findPowerSource((ServerWorld) this.world, obeliskCenter, this.obeliskWidth * 5)) {
                this.applyPlayerEffects();
            }
        }
    }

    private void applyPlayerEffects() {
        Preconditions.checkState(this.world != null);
        Preconditions.checkState(this.obeliskWidth > 0);
        Preconditions.checkState(this.obeliskHeight > 0);

        double range = this.obeliskWidth * 10 + 10;
        Box box = (new Box(this.pos, this.pos.add(obeliskWidth - 1, obeliskHeight - 1, obeliskWidth - 1))).expand(range);

        int effectDuration = (9 + this.obeliskWidth * 2) * 20;
        List<PlayerEntity> players = this.world.getNonSpectatingEntities(PlayerEntity.class, box);

        for (PlayerEntity player : players) {
            if (RemnantComponent.get(player).getRemnantType().isDemon()) {
                for (Object2IntMap.Entry<StatusEffect> effect : this.levels.object2IntEntrySet()) {
                    player.addStatusEffect(new StatusEffectInstance(effect.getKey(), effectDuration, effect.getIntValue() - 1, true, true));
                }
            }
        }
    }

    private boolean findPowerSource(ServerWorld world, Vec3d center, double range) {
        BlockPos.Mutable checked = new BlockPos.Mutable();

        for (int attempt = 0; attempt < RunicObsidianBlockEntity.POWER_ATTEMPTS; attempt++) {
            // https://stackoverflow.com/questions/5837572/generate-a-random-point-within-a-circle-uniformly/50746409#50746409
            double r = range * Math.sqrt(world.random.nextDouble());
            double theta = world.random.nextDouble() * 2 * Math.PI;
            double x = center.x + r * Math.cos(theta);
            double z = center.z + r * Math.sin(theta);
            checked.set(Math.round(x), center.y, Math.round(z));
            BlockState state = world.getBlockState(checked);

            while (!state.isSolidBlock(world, checked)) {
                checked.move(Direction.DOWN);
                state = world.getBlockState(checked);
            }

            if (state.isIn(BlockTags.SOUL_SPEED_BLOCKS)) {
                Vec3d particleSrc = new Vec3d(checked.getX() + 0.5, checked.getY() + 0.9, checked.getZ() + 0.5);
                Vec3d toObelisk = center.subtract(particleSrc).normalize();
                world.spawnParticles(ParticleTypes.SOUL, particleSrc.x, particleSrc.y, particleSrc.z, 0, toObelisk.x, 1, toObelisk.z, 0.1);
                return true;
            }
        }

        return false;
    }

    private RunicObsidianBlockEntity getDelegate() {
        if (this.world != null && this.delegate != null) {
            BlockEntity blockEntity = this.world.getBlockEntity(this.delegate);
            if (blockEntity instanceof RunicObsidianBlockEntity) {
                return ((RunicObsidianBlockEntity) blockEntity).getDelegate();
            }
        }
        return this;
    }

    public int getRangeLevel() {
        return this.getDelegate().obeliskWidth;
    }

    public int getPowerLevel() {
        return this.getDelegate().obeliskHeight;
    }

    private void refresh() {
        assert this.world != null;
        this.levels.clear();
        this.obeliskWidth = 0;
        this.obeliskHeight = 0;

        BlockPos obeliskOrigin = this.findObeliskOrigin();
        if (obeliskOrigin != null) this.matchObelisk(this.world, obeliskOrigin);
    }

    /**
     * Finds the bottommost northwest corner of an obelisk.
     *
     * <p>If a {@link RunicObsidianBlockEntity} is found along the way, the search will be aborted
     * as obelisks checks should be delegated to that block entity.
     * This method does not check that there is a valid obelisk, however it will abort and return
     * {@code Either.right(Optional.empty)} if the core below this block entity does not conform
     * to expectations.
     *
     * @return either the obelisk origin as a {@link BlockPos},
     * or an {@code Optional} describing a better candidate for obelisk checks.
     */
    @Contract(mutates = "this")
    private @Nullable BlockPos findObeliskOrigin() {
        Preconditions.checkState(this.world != null);

        // getBlockEntity is faster than getBlockState, and we know that adjacent blocks must be of the same type for the structure to be valid
        if (!(world.getBlockEntity(this.pos.west()) instanceof RunicObsidianBlockEntity)) {
            if (!(world.getBlockEntity(this.pos.north()) instanceof RunicObsidianBlockEntity)) {
                // we are at the northwest corner, now we go down until we find either the floor or a better candidate
                // this should not go on for too many blocks, so mutable blockpos should not be necessary
                BlockPos obeliskOrigin = this.pos;
                BlockPos down = obeliskOrigin.down();
                BlockState downState = world.getBlockState(down);

                while (!downState.isIn(RequiemBlockTags.OBELISK_FRAME)) {
                    if (!downState.isIn(RequiemBlockTags.OBELISK_CORE)) {
                        // This is not a valid obelisk
                        return null;
                    }
                    obeliskOrigin = down;
                    down = obeliskOrigin.down();

                    if (world.getBlockEntity(down) instanceof RunicObsidianBlockEntity) {
                        // Found a better candidate in a lower core layer
                        this.delegate = down;
                        return null;
                    }

                    downState = world.getBlockState(down);
                }

                return obeliskOrigin;
            } else {
                // Found a better candidate
                this.delegate = this.pos.north();
                return null;
            }
        } else {
            // Found a better candidate
            this.delegate = this.pos.west();
            return null;
        }
    }

    @Contract(mutates = "this")
    private void matchObelisk(World world, BlockPos origin) {
        int tentativeWidth = matchObeliskBase(world, origin);
        if (tentativeWidth > 0) {
            Object2IntMap<StatusEffect> levels = new Object2IntOpenHashMap<>();
            int tentativeHeight = matchObeliskCore(world, origin, tentativeWidth, levels);
            if (tentativeHeight > 0 && matchObeliskCap(world, origin, tentativeWidth, tentativeHeight)) {
                this.obeliskWidth = tentativeWidth;
                this.obeliskHeight = tentativeHeight;
                this.levels.putAll(levels);
            }
        }
    }

    private static int matchObeliskCore(World world, BlockPos origin, int width, Object2IntMap<StatusEffect> levels) {
        // start at north-west corner
        BlockPos start = origin.add(-1, 0, -1);
        BlockPos.Mutable pos = start.mutableCopy();
        int height = 0;

        while (true) {
            if (!world.getBlockState(pos.set(start.getX(), start.getY() + height, start.getZ())).isIn(RequiemBlockTags.OBELISK_FRAME)
                || !world.getBlockState(pos.set(start.getX() + width + 1, start.getY() + height, start.getZ())).isIn(RequiemBlockTags.OBELISK_FRAME)
                || !world.getBlockState(pos.set(start.getX() + width + 1, start.getY() + height, start.getZ() + width + 1)).isIn(RequiemBlockTags.OBELISK_FRAME)
                || !world.getBlockState(pos.set(start.getX(), start.getY() + height, start.getZ() + width + 1)).isIn(RequiemBlockTags.OBELISK_FRAME)
            ) {
                return height;
            }

            @Nullable RuneSearchResult result = findRune(world, origin, width, height);
            if (!result.valid) return height;
            if (result.rune != null && levels.getInt(result.rune.getEffect()) <= result.rune.getMaxLevel()) {
                levels.mergeInt(result.rune.getEffect(), 1, Integer::sum);
            }
            height++;
        }
    }

    private static RuneSearchResult findRune(World world, BlockPos origin, int width, int height) {
        ObeliskRune rune = null;
        boolean first = true;
        for (BlockPos corePos : iterateCoreBlocks(origin, width, height)) {
            BlockState state = world.getBlockState(corePos);
            if (state.isIn(RequiemBlockTags.OBELISK_CORE)) {
                @Nullable ObeliskRune foundRune = ObeliskRune.LOOKUP.find(world, corePos, state, null, null);
                if (first) {
                    rune = foundRune;
                    first = false;
                } else if (foundRune != rune) {
                    return RuneSearchResult.FAILED;
                }
            } else {
                return RuneSearchResult.FAILED;
            }
        }
        return RuneSearchResult.of(rune);
    }

    private static int matchObeliskBase(BlockView world, BlockPos origin) {
        return checkObeliskExtremity(world, origin.add(-1, -1, -1), state -> state.isIn(RequiemBlockTags.OBELISK_FRAME));
    }

    private static boolean matchObeliskCap(BlockView world, BlockPos origin, int width, int height) {
        if (checkObeliskExtremity(world, origin.add(-1, height, -1), state -> {
            if (state.isOf(RequiemBlocks.POLISHED_OBSIDIAN_STAIRS)) {
                StairShape stairShape = state.get(StairsBlock.SHAPE);
                return stairShape == StairShape.OUTER_LEFT || stairShape == StairShape.OUTER_RIGHT;
            }
            return false;
        }) != width) {
            return false;
        }
        for (BlockPos blockPos : iterateCoreBlocks(origin, width, height + 1)) {
            if (!world.getBlockState(blockPos).isOf(RequiemBlocks.POLISHED_OBSIDIAN_SLAB)) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    private static Iterable<BlockPos> iterateCoreBlocks(BlockPos origin, int width, int height) {
        return BlockPos.iterate(origin.up(height), origin.add(width - 1, height, width - 1));
    }

    private static int checkObeliskExtremity(BlockView world, BlockPos origin, Predicate<BlockState> corner) {
        // start at bottom north-west corner
        BlockPos.Mutable pos = origin.mutableCopy();
        int lastSideLength = -1;

        for (Direction direction : OBELISK_SIDES) {
            int sideLength = 0;
            while (true) {
                BlockState state = world.getBlockState(pos);
                if (corner.test(state)) {
                    // Corner block can be start or end of side
                    if (sideLength > 0) {
                        break;
                    }
                } else if (state.isOf(RequiemBlocks.POLISHED_OBSIDIAN_STAIRS)) {
                    sideLength++;
                    if (sideLength > 5) {
                        // Too large: not a valid base
                        return 0;
                    }
                } else {
                    // Unexpected block: not a valid base
                    return 0;
                }
                pos.move(direction);
            }
            if (lastSideLength < 0) {
                lastSideLength = sideLength;
            } else if (lastSideLength != sideLength) {
                // Not a square base: not a valid base
                return 0;
            }
        }

        return lastSideLength;
    }

    private static class RuneSearchResult {
        static final RuneSearchResult FAILED = new RuneSearchResult(false, null);
        static final RuneSearchResult INERT = new RuneSearchResult(true, null);

        static RuneSearchResult of(@Nullable ObeliskRune rune) {
            return rune == null ? INERT : new RuneSearchResult(true, rune);
        }

        final boolean valid;
        final @Nullable ObeliskRune rune;

        RuneSearchResult(boolean valid, @Nullable ObeliskRune rune) {
            this.valid = valid;
            this.rune = rune;
        }
    }
}
