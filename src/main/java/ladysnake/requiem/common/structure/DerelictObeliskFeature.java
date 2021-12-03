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
package ladysnake.requiem.common.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import ladysnake.requiem.Requiem;
import net.minecraft.block.BlockState;
import net.minecraft.class_6621;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.Collectors;

public class DerelictObeliskFeature extends StructureFeature<DefaultFeatureConfig> {

    public DerelictObeliskFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec, context -> {
                // Check if the spot is valid for structure gen. If false, return nothing to signal to the game to skip this spawn attempt.
                if (!canGenerate(context)) {
                    return Optional.empty();
                }
                // Create the pieces layout of the structure and give it to
                else {
                    return createPiecesGenerator(context);
                }
            },
            class_6621.field_34938);
    }

    @Override
    public StructureStartFactory<DefaultFeatureConfig> getStructureStartFactory() {
        return Start::new;
    }

    /**
     * Stolen from {@link net.minecraft.world.gen.feature.RuinedPortalFeature}
     */
    static OptionalInt getFloorHeight(Random random, ChunkGenerator chunkGenerator, BlockBox box, HeightLimitView world) {
        int maxY = MathHelper.nextBetween(random, 60, 100);

        List<BlockPos> corners = ImmutableList.of(new BlockPos(box.getMinX(), 0, box.getMinZ()), new BlockPos(box.getMaxX(), 0, box.getMinZ()), new BlockPos(box.getMinX(), 0, box.getMaxZ()), new BlockPos(box.getMaxX(), 0, box.getMaxZ()));
        List<VerticalBlockSample> cornerColumns = corners.stream().map(blockPos -> chunkGenerator.getColumnSample(blockPos.getX(), blockPos.getZ(), world)).collect(Collectors.toList());
        Heightmap.Type heightmapType = Heightmap.Type.OCEAN_FLOOR_WG;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        int y;
        for (y = maxY; y > 15; --y) {
            int validCorners = 0;
            pos.set(0, y, 0);

            for (VerticalBlockSample cornerColumn : cornerColumns) {
                BlockState blockState = cornerColumn.getState(pos);
                if (heightmapType.getBlockPredicate().test(blockState)) {
                    ++validCorners;
                }
            }

            if (validCorners >= 3) {
                validCorners = 0;
                pos.move(Direction.UP, box.getBlockCountY() - 1);

                for (VerticalBlockSample cornerColumn : cornerColumns) {
                    BlockState blockState = cornerColumn.getState(pos);
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

    /**
     * Handles calling up the structure's pieces class and height that structure will spawn at.
     */
    public static class Start extends StructureStart<DefaultFeatureConfig> {
        public Start(StructureFeature<DefaultFeatureConfig> structureIn, ChunkPos chunkPos, int referenceIn, long seedIn) {
            super(structureIn, chunkPos, referenceIn, seedIn);
        }

        @Override
        public void init(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, StructureManager structureManager, ChunkPos chunkPos, Biome biome, DefaultFeatureConfig config, HeightLimitView world) {
            // Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
            int x = chunkPos.x * 16;
            int z = chunkPos.z * 16;
            BlockPos.Mutable centerPos = new BlockPos.Mutable(x, 0, z);

            StructurePoolFeatureConfig structureSettingsAndStartPool = new StructurePoolFeatureConfig(
                () -> dynamicRegistryManager.get(Registry.STRUCTURE_POOL_KEY).get(Requiem.id("derelict_obelisk")), 1
            );

            StructurePoolElement spawnedStructure = structureSettingsAndStartPool.getStartPool().get().getRandomElement(random);

            if (spawnedStructure != EmptyPoolElement.INSTANCE) {
                BlockRotation rotation = Util.getRandom(BlockRotation.values(), this.random);
                BlockPos startPos = chunkPos.getStartPos();
                PoolStructurePiece piece = new PoolStructurePiece(
                    structureManager,
                    spawnedStructure,
                    startPos,
                    spawnedStructure.getGroundLevelDelta(),
                    rotation,
                    spawnedStructure.getBoundingBox(structureManager, startPos, rotation)
                );
                BlockBox boundingBox = piece.getBoundingBox();
                OptionalInt floorY = getFloorHeight(this.random, chunkGenerator, boundingBox, world);

                if (floorY.isEmpty()) return;

                int lowering = boundingBox.getMinY() + piece.getGroundLevelDelta();
                piece.translate(0, floorY.getAsInt() - lowering, 0);
                this.addPiece(piece);

                // Since by default, the start piece of a structure spawns with its corner at centerPos
                // and will randomly rotate around that corner, we will center the piece on centerPos instead.
                // This is so that our structure's start piece is now centered on the water check done in shouldStartAt.
                // Whatever the offset done to center the start piece, that offset is applied to all other pieces
                // so the entire structure is shifted properly to the new spot.
                Vec3i structureCenter = this.children.get(0).getBoundingBox().getCenter();
                int xOffset = centerPos.getX() - structureCenter.getX();
                int zOffset = centerPos.getZ() - structureCenter.getZ();
                for (StructurePiece structurePiece : this.children) {
                    structurePiece.translate(xOffset, 0, zOffset);
                }

                // Sets the bounds of the structure once you are finished.
                this.setBoundingBoxFromChildren();
            }
        }
    }
}
