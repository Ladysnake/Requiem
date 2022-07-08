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
package ladysnake.requiem.common.block.obelisk;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ladysnake.requiem.api.v1.block.ObeliskRune;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.block.RequiemBlockEntities;
import ladysnake.requiem.common.block.RequiemBlocks;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.util.ObeliskDescriptor;
import net.minecraft.block.BlockState;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RunestoneBlockEntity extends BaseRunestoneBlockEntity {
    public static final int POWER_ATTEMPTS = 6;

    private final Object2IntMap<ObeliskRune> levels = new Object2IntOpenHashMap<>();
    private @Nullable UUID recordUuid;
    private int obeliskCoreWidth = 0;
    private int obeliskCoreHeight = 0;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(RequiemBlockEntities.RUNIC_OBSIDIAN, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RunestoneBlockEntity be) {
        assert !world.isClient();

        if (be.previousPowerRate != be.powerRate) {
            be.previousPowerRate = be.powerRate;
            RequiemNetworking.sendObeliskPowerUpdateMessage(be);
        }

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

            be.updatePower((ServerWorld) world, obeliskCenter, getRange(obeliskWidth));

            if (!be.levels.isEmpty() && be.isPowered()) {
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

    private void updatePower(ServerWorld world, Vec3d center, double range) {
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

        this.setPowerRate((float) successes / POWER_ATTEMPTS);
        RequiemNetworking.sendObeliskPowerUpdateMessage(this);
    }

    private static void spawnSoul(ServerWorld world, Vec3d center, Vec3d particleSrc) {
        Vec3d toObelisk = center.subtract(particleSrc).normalize();
        world.spawnParticles(RequiemParticleTypes.OBELISK_SOUL, particleSrc.x, particleSrc.y, particleSrc.z, 0, toObelisk.x, 1, toObelisk.z, 0.1);
    }

    public int getCoreWidth() {
        return this.obeliskCoreWidth;
    }

    public int getCoreHeight() {
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

        ObeliskMatcher.matchObelisk(this.world, this.pos).result()
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
                () -> this.world.scheduleBlockTick(this.pos, state.getBlock(), 0)
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
            return player.squaredDistanceTo((double) this.pos.getX() + 0.5, (double) this.pos.getY() + 0.5, (double) this.pos.getZ() + 0.5) <= 64.0;
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

        if (nbt.containsUuid("linked_record")) {
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
}
