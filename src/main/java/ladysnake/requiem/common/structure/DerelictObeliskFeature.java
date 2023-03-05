/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.common.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.StructureType;
import net.minecraft.structure.piece.PoolStructurePiece;
import net.minecraft.structure.piece.StructurePiece;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Holder;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.RandomState;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class DerelictObeliskFeature extends StructureFeature {
    public static final Codec<DerelictObeliskFeature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        settingsCodec(instance),
        StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool)
    ).apply(instance, DerelictObeliskFeature::new));

    private final Holder<StructurePool> startPool;

    public DerelictObeliskFeature(StructureSettings settings, Holder<StructurePool> startPool) {
        super(settings);
        this.startPool = startPool;
    }

    /**
     * Stolen from {@link net.minecraft.world.gen.feature.RuinedPortalFeature}
     */
    static OptionalInt getFloorHeight(RandomGenerator random, RandomState randomState, ChunkGenerator chunkGenerator, BlockBox box, HeightLimitView world) {
        int maxY = MathHelper.nextBetween(random, 60, 100);

        List<BlockPos> corners = ImmutableList.of(new BlockPos(box.getMinX(), 0, box.getMinZ()), new BlockPos(box.getMaxX(), 0, box.getMinZ()), new BlockPos(box.getMinX(), 0, box.getMaxZ()), new BlockPos(box.getMaxX(), 0, box.getMaxZ()));
        List<VerticalBlockSample> cornerColumns = corners.stream().map(blockPos -> chunkGenerator.getColumnSample(blockPos.getX(), blockPos.getZ(), world, randomState)).toList();
        Heightmap.Type heightmapType = Heightmap.Type.OCEAN_FLOOR_WG;

        int y;
        for (y = maxY; y > 15; --y) {
            int validCorners = 0;

            for (VerticalBlockSample cornerColumn : cornerColumns) {
                BlockState blockState = cornerColumn.getState(y);
                if (heightmapType.getBlockPredicate().test(blockState)) {
                    ++validCorners;
                }
            }

            if (validCorners >= 3) {
                validCorners = 0;

                for (VerticalBlockSample cornerColumn : cornerColumns) {
                    BlockState blockState = cornerColumn.getState(y + box.getBlockCountY() - 1);
                    if (blockState.isAir()) {
                        ++validCorners;
                        if (validCorners == 2) {
                            return OptionalInt.of(y + 1);
                        }
                    }
                }
            }
        }

        return OptionalInt.empty();
    }

    @Override
    public Optional<GenerationStub> findGenerationPos(GenerationContext context) {
        // Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.x * 16;
        int z = chunkPos.z * 16;

        BlockPos.Mutable centerPos = new BlockPos.Mutable(x, 0, z);

        return Optional.of(new GenerationStub(centerPos, structurePieces -> {
            ChunkRandom chunkRandom = context.random();
            StructurePoolElement spawnedStructure = this.startPool.value().getRandomElement(chunkRandom);

            if (spawnedStructure != EmptyPoolElement.INSTANCE) {
                BlockRotation rotation = Util.getRandom(BlockRotation.values(), chunkRandom);
                BlockPos startPos = chunkPos.getStartPos();
                StructureTemplateManager structureManager = context.structureTemplateManager();
                PoolStructurePiece piece = new PoolStructurePiece(
                    structureManager,
                    spawnedStructure,
                    startPos,
                    spawnedStructure.getGroundLevelDelta(),
                    rotation,
                    spawnedStructure.getBoundingBox(structureManager, startPos, rotation)
                );
                BlockBox boundingBox = piece.getBoundingBox();
                OptionalInt floorY = getFloorHeight(context.random(), context.randomState(), context.chunkGenerator(), boundingBox, context.world());

                if (floorY.isEmpty()) return;

                int lowering = boundingBox.getMinY() + piece.getGroundLevelDelta();
                piece.translate(0, floorY.getAsInt() - lowering, 0);
                structurePieces.addPiece(piece);

                // Since by default, the start piece of a structure spawns with its corner at centerPos
                // and will randomly rotate around that corner, we will center the piece on centerPos instead.
                // This is so that our structure's start piece is now centered on the water check done in shouldStartAt.
                // Whatever the offset done to center the start piece, that offset is applied to all other pieces
                // so the entire structure is shifted properly to the new spot.
                Vec3i structureCenter = structurePieces.toList().pieces().get(0).getBoundingBox().getCenter();
                int xOffset = centerPos.getX() - structureCenter.getX();
                int zOffset = centerPos.getZ() - structureCenter.getZ();
                for (StructurePiece structurePiece : structurePieces.toList().pieces()) {
                    structurePiece.translate(xOffset, 0, zOffset);
                }
            }
        }));
    }

    @Override
    public StructureType<?> getType() {
        return RequiemStructures.DERELICT_OBELISK;
    }
}
