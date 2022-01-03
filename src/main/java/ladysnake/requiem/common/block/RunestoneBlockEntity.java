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

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ladysnake.requiem.api.v1.block.ObeliskRune;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.tag.RequiemBlockTags;
import ladysnake.requiem.common.util.ObeliskDescriptor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class RunestoneBlockEntity extends InertRunestoneBlockEntity {
    public static final Direction[] OBELISK_SIDES = {Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
    public static final int POWER_ATTEMPTS = 6;
    public static final int MAX_OBELISK_CORE_WIDTH = 5;
    public static final int MAX_OBELISK_CORE_HEIGHT = 20;
    public static final DataResult<ObeliskMatch> INVALID_BASE = DataResult.error("Structure does not have a matching base");
    public static final DataResult<ObeliskMatch> INVALID_CORE = DataResult.error("Structure does not have a matching runic core");
    public static final DataResult<ObeliskMatch> INVALID_CAP = DataResult.error("Structure does not have a matching cap");
    public static final int NO_MATCH = 0;

    private final Object2IntMap<ObeliskRune> levels = new Object2IntOpenHashMap<>();
    private @Nullable UUID recordUuid;
    private int obeliskCoreWidth = 0;
    private int obeliskCoreHeight = 0;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(RequiemBlockEntities.RUNIC_OBSIDIAN, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RunestoneBlockEntity be) {
        if (world.isClient) return;

        // Salt the time to avoid checking every potential obelisk on the same tick
        if ((world.getTime() + pos.hashCode()) % 80L == 0L) {
            if (!state.get(InertRunestoneBlock.HEAD)) {
                world.removeBlockEntity(pos);
                be.onDestroyed();
                return;
            }

            be.refreshStructure(state);

            int obeliskWidth = be.obeliskCoreWidth;
            Vec3d obeliskCenter = getObeliskCenter(pos, obeliskWidth);

            if (!be.levels.isEmpty() && findPowerSource((ServerWorld) world, obeliskCenter, getRange(obeliskWidth))) {
                be.applyPlayerEffects(world, pos);
                world.playSound(null, pos, RequiemSoundEvents.BLOCK_OBELISK_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.4F);
            }
        }
    }

    private static int getRange(int obeliskWidth) {
        return obeliskWidth * 5;
    }

    private static Vec3d getObeliskCenter(BlockPos origin, int coreWidth) {
        return new Vec3d(
            MathHelper.lerp(0.5, origin.getX(), origin.getX() + coreWidth - 1),
            origin.getY() - 2,
            MathHelper.lerp(0.5, origin.getZ(), origin.getZ() + coreWidth - 1)
        );
    }

    public static Optional<BlockPos> findObeliskOrigin(World world, BlockPos pos) {
        while (true) {
            ObeliskOriginMatch match = tryMatchObeliskOrigin(world, pos);
            if (match.origin != null) return Optional.of(match.origin);
            else if (match.delegate == null) return Optional.empty();
            else pos = match.delegate;
        }
    }

    private void applyPlayerEffects(World world, BlockPos pos) {
        Preconditions.checkState(this.obeliskCoreWidth > 0);
        Preconditions.checkState(this.obeliskCoreHeight > 0);

        double range = this.obeliskCoreWidth * 10 + 10;
        Box box = (new Box(pos, pos.add(this.obeliskCoreWidth - 1, obeliskCoreHeight - 1, this.obeliskCoreWidth - 1))).expand(range);

        List<PlayerEntity> players = world.getNonSpectatingEntities(PlayerEntity.class, box);

        for (PlayerEntity player : players) {
            if (RemnantComponent.get(player).getRemnantType().isDemon()) {
                for (Object2IntMap.Entry<ObeliskRune> effect : this.levels.object2IntEntrySet()) {
                    effect.getKey().applyEffect((ServerPlayerEntity) player, effect.getIntValue(), this.obeliskCoreWidth);
                }
            }
        }
    }

    public static boolean checkForPower(ServerWorld world, BlockPos origin) {
        return matchObelisk(world, origin).result().map(match -> findPowerSource(
            world,
            getObeliskCenter(match.origin(), match.coreWidth()),
            getRange(match.coreWidth()))
        ).orElse(Boolean.FALSE);
    }

    private static boolean findPowerSource(ServerWorld world, Vec3d center, double range) {
        BlockPos.Mutable checked = new BlockPos.Mutable();
        int successes = 0;

        for (int attempt = 0; attempt < RunestoneBlockEntity.POWER_ATTEMPTS; attempt++) {
            double x = center.x + world.random.nextDouble() * range * 2 - range;
            double z = center.z + world.random.nextDouble() * range * 2 - range;
            checked.set(Math.round(x), center.y, Math.round(z));
            BlockState state = world.getBlockState(checked);

            while (!state.isSolidBlock(world, checked)) {
                checked.move(Direction.DOWN);
                state = world.getBlockState(checked);
            }

            if (state.isIn(BlockTags.SOUL_SPEED_BLOCKS)) {
                spawnSoul(world, center, Vec3d.ofCenter(checked, 0.9));
                successes++;
            }
        }

        // Require more than half of the attempts to be successful
        return successes > POWER_ATTEMPTS / 2;
    }

    private static void spawnSoul(ServerWorld world, Vec3d center, Vec3d particleSrc) {
        Vec3d toObelisk = center.subtract(particleSrc).normalize();
        world.spawnParticles(RequiemParticleTypes.OBELISK_SOUL, particleSrc.x, particleSrc.y, particleSrc.z, 0, toObelisk.x, 1, toObelisk.z, 0.1);
    }

    public int getRangeLevel() {
        return this.obeliskCoreWidth;
    }

    public int getPowerLevel() {
        return this.obeliskCoreHeight;
    }

    public Optional<ObeliskDescriptor> getDescriptor() {
        if (this.world != null && this.recordUuid != null) {
            return GlobalRecordKeeper.get(this.world).getRecord(this.recordUuid).flatMap(r -> r.get(RequiemRecordTypes.OBELISK_REF));
        }

        return Optional.empty();
    }

    private void refreshStructure(BlockState state) {
        assert this.world != null;
        this.levels.clear();
        this.obeliskCoreWidth = 0;
        this.obeliskCoreHeight = 0;

        matchObelisk(this.world, this.pos).result()
            .ifPresentOrElse(
                match -> {
                    Object2IntMap<ObeliskRune> runes = match.collectRunes();
                    this.obeliskCoreWidth = match.coreWidth();
                    this.obeliskCoreHeight = match.coreHeight();
                    this.levels.putAll(runes);
                    Optional<Text> customName = match.names().stream().unordered().findAny();
                    if (this.recordUuid == null && runes.containsKey(RequiemBlocks.RIFT_RUNE)) {
                        // Clear leftover global records, should not be needed but uuuh bugs
                        GlobalRecordKeeper.get(this.world).getRecords()
                            .stream()
                            .filter(r -> r.get(RequiemRecordTypes.OBELISK_REF)
                                .map(ObeliskDescriptor::pos)
                                .filter(this.pos::equals).isPresent())
                            .forEach(GlobalRecord::invalidate);
                        GlobalRecord record = GlobalRecordKeeper.get(this.world).createRecord();
                        record.put(RequiemRecordTypes.OBELISK_REF, new ObeliskDescriptor(
                            this.world.getRegistryKey(),
                            this.getPos(),
                            this.obeliskCoreWidth,
                            this.obeliskCoreHeight,
                            customName.or(this::generateName)
                        ));
                        record.put(RequiemRecordTypes.RIFT_OBELISK, Unit.INSTANCE);
                        this.recordUuid = record.getUuid();
                    }
                },
                () -> this.world.createAndScheduleBlockTick(this.pos, state.getBlock(), 0)
            );
    }

    private Optional<Text> generateName() {
        // TODO name generation
        return Optional.empty();
    }

    public boolean canBeUsedBy(PlayerEntity player) {
        if (player.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return !(player.squaredDistanceTo((double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.5, (double)this.pos.getZ() + 0.5) > 64.0);
        }
    }

    public void onDestroyed() {
        if (this.recordUuid != null && this.getWorld() != null) {
            GlobalRecordKeeper.get(this.getWorld()).getRecord(this.recordUuid).ifPresent(GlobalRecord::invalidate);
            this.recordUuid = null;
        }

        if (this.world != null) {
            this.world.playSound(null, pos, RequiemSoundEvents.BLOCK_OBELISK_DEACTIVATE, SoundCategory.BLOCKS, 1, 0.3f);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.contains("linked_record")) {
            this.recordUuid = nbt.getUuid("linked_record");
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (this.recordUuid != null) {
            nbt.putUuid("linked_record", this.recordUuid);
        }
    }

    /**
     * Finds the bottommost northwest corner of an obelisk.
     *
     * <p>If a {@link RunestoneBlockEntity} is found along the way, the search will be aborted
     * and a potential delegate will be set.
     * This method does not check that there is a valid obelisk, however it will abort and return
     * {@code null} if the core below this block entity does not conform
     * to expectations.
     *
     * @param world the world in which to search for the origin of an obelisk
     * @param pos   the position from which to initiate the search
     * @return an {@link Either} describing either a potential origin for an obelisk, or the position of a better-suited delegate
     */
    public static ObeliskOriginMatch tryMatchObeliskOrigin(World world, BlockPos pos) {
        if (!(world.getBlockState(pos.west()).isIn(RequiemBlockTags.OBELISK_CORE))) {
            if (!(world.getBlockState(pos.north()).isIn(RequiemBlockTags.OBELISK_CORE))) {
                // we are at the northwest corner, now we go down until we find either the floor or a better candidate
                // this should not go on for too many blocks, so mutable blockpos should not be necessary
                BlockPos obeliskOrigin = pos;
                BlockPos down = obeliskOrigin.down();
                BlockState downState = world.getBlockState(down);

                while (downState.isIn(RequiemBlockTags.OBELISK_CORE)) {
                    obeliskOrigin = down;
                    down = obeliskOrigin.down();

                    if (world.getBlockEntity(down) instanceof RunestoneBlockEntity) {
                        // Found a better candidate in a lower core layer
                        return ObeliskOriginMatch.partial(down);
                    }

                    downState = world.getBlockState(down);
                }

                return ObeliskOriginMatch.success(obeliskOrigin);
            } else {
                // Found a better candidate in the same layer
                return ObeliskOriginMatch.partial(pos.north());
            }
        } else {
            // Found a better candidate in the same layer
            return ObeliskOriginMatch.partial(pos.west());
        }
    }

    public static DataResult<ObeliskMatch> matchObelisk(World world, BlockPos origin) {
        int tentativeWidth = getObeliskCoreWidth(world, origin);
        if (tentativeWidth != NO_MATCH && matchObeliskBase(world, origin, tentativeWidth)) {
            ObeliskMatch attempt = matchObeliskCore(world, origin, tentativeWidth);
            if (attempt.coreHeight() > 0) {
                if (matchObeliskCap(world, origin, tentativeWidth, attempt.coreHeight())) {
                    return DataResult.success(attempt);
                }
                return INVALID_CAP;
            }
            return INVALID_CORE;
        }
        return INVALID_BASE;
    }

    private static int getObeliskCoreWidth(World world, BlockPos origin) {
        // We assume that the origin got already tested
        int sideLength = 1;

        while (world.getBlockState(origin.offset(OBELISK_SIDES[0], sideLength)).isIn(RequiemBlockTags.OBELISK_CORE)) {
            sideLength++;

            if (sideLength > MAX_OBELISK_CORE_WIDTH) {
                // Too large: not a valid obelisk
                return NO_MATCH;
            }
        }

        return sideLength;
    }

    private static ObeliskMatch matchObeliskCore(World world, BlockPos origin, int coreWidth) {
        // start at north-west corner
        BlockPos start = origin.add(-1, 0, -1);
        BlockPos.Mutable pos = start.mutableCopy();
        List<RuneSearchResult> layers = new ArrayList<>();
        Set<Text> names = new HashSet<>();
        int height;

        for (height = 0; height < MAX_OBELISK_CORE_HEIGHT; height++) {
            if (!testCoreLayerFrame(world, start, pos, coreWidth, height)) break;
            RuneSearchResult result = findRune(world, origin, coreWidth, height);
            if (!result.valid()) break;
            layers.add(result);
            names.addAll(result.names());
        }

        return new ObeliskMatch(origin, coreWidth, height, layers, names);
    }

    private static boolean testCoreLayerFrame(World world, BlockPos start, BlockPos.Mutable pos, int width, int height) {
        return world.getBlockState(pos.set(start.getX(), start.getY() + height, start.getZ())).isIn(RequiemBlockTags.OBELISK_FRAME)
            && world.getBlockState(pos.set(start.getX() + width + 1, start.getY() + height, start.getZ())).isIn(RequiemBlockTags.OBELISK_FRAME)
            && world.getBlockState(pos.set(start.getX() + width + 1, start.getY() + height, start.getZ() + width + 1)).isIn(RequiemBlockTags.OBELISK_FRAME)
            && world.getBlockState(pos.set(start.getX(), start.getY() + height, start.getZ() + width + 1)).isIn(RequiemBlockTags.OBELISK_FRAME);
    }

    private static RuneSearchResult findRune(World world, BlockPos origin, int width, int height) {
        Set<Text> names = new HashSet<>();
        ObeliskRune rune = null;
        boolean first = true;

        for (BlockPos corePos : iterateCoreBlocks(origin, width, height)) {
            BlockState state = world.getBlockState(corePos);
            if (state.isIn(RequiemBlockTags.OBELISK_CORE)) {
                BlockEntity be = state.hasBlockEntity() ? world.getBlockEntity(corePos) : null;
                @Nullable ObeliskRune foundRune = ObeliskRune.LOOKUP.find(world, corePos, state, be, null);
                if (first) {
                    rune = foundRune;
                    first = false;
                    if (be instanceof InertRunestoneBlockEntity runestone) runestone.getCustomName().ifPresent(names::add);
                } else if (foundRune != rune) {
                    return RuneSearchResult.FAILED;
                }
            } else {
                return RuneSearchResult.FAILED;
            }
        }

        return RuneSearchResult.of(rune, names);
    }

    private static boolean matchObeliskBase(BlockView world, BlockPos origin, int coreWidth) {
        return checkObeliskExtremity(world, origin.add(-1, -1, -1), coreWidth + 2);
    }

    private static boolean matchObeliskCap(BlockView world, BlockPos origin, int coreWidth, int height) {
        return checkObeliskExtremity(world, origin.add(-1, height, -1), coreWidth + 2);
    }

    static Iterable<BlockPos> iterateCoreBlocks(BlockPos origin, int coreWidth, int height) {
        return BlockPos.iterate(origin.up(height), origin.add(coreWidth - 1, height, coreWidth - 1));
    }

    private static boolean checkObeliskExtremity(BlockView world, BlockPos origin, int expectedWidth) {
        // We start at bottom north-west corner
        BlockPos.Mutable pos = origin.mutableCopy();

        for (Direction direction : OBELISK_SIDES) {
            int sideLength = 0;

            while (true) {
                if (!world.getBlockState(pos).isIn(RequiemBlockTags.OBELISK_FRAME)) {
                    return false;
                }

                sideLength++;

                if (sideLength < expectedWidth) {
                    pos.move(direction);
                } else {
                    break;
                }
            }
        }

        return true;
    }

    record RuneSearchResult(boolean valid, @Nullable ObeliskRune rune, Set<Text> names) {
        static final RuneSearchResult FAILED = new RuneSearchResult(false, null, Set.of());
        static final RuneSearchResult INERT = new RuneSearchResult(true, null, Set.of());

        static RuneSearchResult of(@Nullable ObeliskRune rune, Set<Text> names) {
            return rune == null && names.isEmpty() ? INERT : new RuneSearchResult(true, rune, names);
        }
    }

    private static final class ObeliskOriginMatch {
        private static final ObeliskOriginMatch FAIL = new ObeliskOriginMatch(null, null);

        private final @Nullable BlockPos origin;
        private final @Nullable BlockPos delegate;

        public static ObeliskOriginMatch success(BlockPos origin) {
            return new ObeliskOriginMatch(origin, null);
        }

        public static ObeliskOriginMatch partial(BlockPos delegate) {
            return new ObeliskOriginMatch(null, delegate);
        }

        public static ObeliskOriginMatch failure() {
            return FAIL;
        }

        private ObeliskOriginMatch(@Nullable BlockPos origin, @Nullable BlockPos delegate) {
            this.origin = origin;
            this.delegate = delegate;
        }

        public void apply(Consumer<BlockPos> originConsumer, Consumer<BlockPos> delegateConsumer, Runnable failure) {
            if (this.origin != null) originConsumer.accept(this.origin);
            else if (this.delegate != null) delegateConsumer.accept(this.delegate);
        }
    }
}
