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
package ladysnake.pandemonium.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Some raytracing utilities
 *
 * @author UpcraftLP
 */
public class RayHelper {

    /**
     * Raytraces using an entity's position as source, and its look vector as direction
     *
     * @param entity        the raytrace source
     * @param range         the maximum range of the raytrace
     * @param shapeType     <b>COLLIDER</b> for collision raytracing, <b>OUTLINE</b> for tracing the block outline shape (render bounding box)
     * @param fluidHandling how to handle fluids
     * @param tickDeltaTime the delta tick time (partial render tick)
     */
    @Nonnull
    public static HitResult raycastEntity(Entity entity, double range, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, float tickDeltaTime) {
        Vec3d startPoint = entity.getCameraPosVec(tickDeltaTime);
        Vec3d lookVec = entity.getRotationVec(tickDeltaTime);
        Vec3d endPoint = startPoint.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
        return raycast(entity.world, entity, startPoint, endPoint, shapeType, fluidHandling);
    }

    /**
     *
     * @param world         the world
     * @param source        the entity to be used for determining block bounding boxes
     * @param start         the start point
     * @param end           the end point, if no result was found
     * @param shapeType     <b>COLLIDER</b> for collision raytracing, <b>OUTLINE</b> for tracing the block outline shape (render bounding box)
     * @param fluidHandling how to handle fluids
     */
    @Nonnull
    public static HitResult raycast(World world, Entity source, Vec3d start, Vec3d end, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling) {
        return world.raycast(new RaycastContext(start, end, shapeType, fluidHandling, source));
    }

    /**
     * Finds a suitable blink position for an entity based on its look
     *
     * @param entity    the source of the raytrace
     * @param deltaTime the delta tick time (partial render tick)
     * @param range     the maximum range that the entity can target
     * @return the position targeted by <code>entity</code>
     */
    public static Vec3d findBlinkPos(Entity entity, float deltaTime, double range) {
        World world = entity.world;
        HitResult trace = raycastEntity(entity, range, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY, deltaTime);
        boolean secondPass;
        if (trace.getType() == HitResult.Type.MISS) {
            trace = raycast(world, entity, trace.getPos(), trace.getPos().subtract(0, 1, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY);
            secondPass = false;
        } else {
            secondPass = true;
        }
        Vec3d pos = trace.getPos();
        if (trace.getType() == HitResult.Type.BLOCK) {
            BlockHitResult result = (BlockHitResult) trace;
            switch (result.getSide()) {
                case DOWN:
                    pos = pos.subtract(0, entity.getHeight(), 0);
                    break;
                case UP:
                    secondPass = false;
                    break;
                default:
                    Vec3d entityPos = entity.getCameraPosVec(deltaTime);
                    Vec3d toTarget = pos.subtract(entityPos);
                    if (pos.y - (int) pos.y >= 0.5D) {
                        BlockPos testPos;
                        switch (result.getSide()) {
                            case EAST:
                                testPos = new BlockPos(pos.x - 1, pos.y + 1, pos.z);
                                break;
                            case WEST:
                            case NORTH:
                                testPos = new BlockPos(pos.x, pos.y + 1, pos.z);
                                break;
                            case SOUTH:
                                testPos = new BlockPos(pos.x, pos.y + 1, pos.z - 1);
                                break;
                            default: //should never happen, but better safe than sorry
                                throw new RaytraceException("hit result had wrong value: " + result.getSide());
                        }
                        if (!world.isSpaceEmpty(null, entity.getBoundingBox().offset(testPos.getX() - entity.getX(), testPos.getY() - entity.getY(), testPos.getZ() - entity.getZ()))) {
                            toTarget = toTarget.multiply(Math.max((toTarget.length() + 0.8D) / toTarget.length(), 1.0D));
                            pos = new Vec3d(entityPos.x + toTarget.x, testPos.getY() + 0.1D, entityPos.z + toTarget.z);
                            HitResult result1 = raycast(world, entity, pos, pos.subtract(0.0D, 1.0D, 0.0D), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY);
                            pos = result1.getPos();
                            secondPass = false;
                        }
                    }
                    if (secondPass) {
                        toTarget = toTarget.multiply((toTarget.length() - (entity.getWidth() * 1.3F)) / toTarget.length());
                        pos = entityPos.add(toTarget);
                        HitResult result1 = raycast(world, entity, pos, pos.subtract(0.0D, 1.0D, 0.0D), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY);
                        pos = result1.getPos();
                    }
            }
        }
        if (secondPass) {
            Vec3d tempPos = pos.subtract(0.0D, 0.0001D, 0.0D);
            HitResult flagcast = raycast(world, entity, tempPos, pos.add(0.0D, entity.getHeight(), 0.0D), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY);
            if (flagcast.getPos().y - tempPos.y < entity.getHeight()) {
                pos = flagcast.getPos().subtract(0, entity.getHeight(), 0);
            }
        }
        return pos;
    }

    /**
     * @see net.minecraft.entity.projectile.ProjectileUtil#raycast(Entity, Vec3d, Vec3d, Box, Predicate, double)
     */
    public static EntityHitResult raycast(Entity watcher, Vec3d startPoint, Vec3d endPoint, Box box, Predicate<Entity> predicate, double range) {
        World world = watcher.world;
        double r = range;
        Entity target = null;
        Vec3d pos = null;

        for (Entity entity : world.getOtherEntities(watcher, box, predicate)) {
            Box bb = entity.getBoundingBox().expand(entity.getTargetingMargin());
            Optional<Vec3d> hitPosition = bb.raycast(startPoint, endPoint);
            if (bb.contains(startPoint)) {
                if (r >= 0.0D) {
                    target = entity;
                    pos = hitPosition.orElse(startPoint);
                    r = 0.0D;
                }
            } else if (hitPosition.isPresent()) {
                Vec3d hitPos = hitPosition.get();
                double distanceSq = startPoint.squaredDistanceTo(hitPos);
                if (distanceSq < r || r == 0.0D) {
                    if (entity.getRootVehicle() == watcher.getRootVehicle()) {
                        if (r == 0.0D) {
                            target = entity;
                            pos = hitPos;
                        }
                    } else {
                        target = entity;
                        pos = hitPos;
                        r = distanceSq;
                    }
                }
            }
        }

        if (target == null) {
            return null;
        }

        return new EntityHitResult(target, pos);
    }

    public static class RaytraceException extends RuntimeException {
        public RaytraceException(String message) {
            super(message);
        }
    }
}
