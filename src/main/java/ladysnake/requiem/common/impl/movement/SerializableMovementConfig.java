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
package ladysnake.requiem.common.impl.movement;

import com.google.gson.Gson;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import ladysnake.requiem.api.v1.entity.movement.SwimMode;
import ladysnake.requiem.api.v1.entity.movement.WalkMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.network.PacketByteBuf;
import org.apiguardian.api.API;

/**
 * A {@link MovementConfig} that can be easily manipulated by {@link Gson} and equivalent.
 */
public class SerializableMovementConfig implements MovementConfig {
    public static final SerializableMovementConfig SOUL = new SerializableMovementConfig(MovementMode.ENABLED, SwimMode.ENABLED, WalkMode.NORMAL, TriState.FALSE, TriState.FALSE, true, 0, 1f, 1f, 0.1F);

    private MovementMode flightMode;
    private SwimMode swimMode;
    private WalkMode walkMode;
    private TriState sinksInWater;
    private TriState flopsOnLand;
    private boolean climbsWalls;
    private boolean phasesThroughWalls;
    private float gravity;
    private float fallSpeedModifier;
    private float walkSpeedModifier;
    private float inertia;

    @CalledThroughReflection
    @API(status = API.Status.INTERNAL)
    public SerializableMovementConfig() {
        this(MovementMode.UNSPECIFIED, SwimMode.UNSPECIFIED, WalkMode.NORMAL, TriState.DEFAULT, TriState.DEFAULT, false, 0, 1f, 1f, 0);
    }

    @API(status = API.Status.INTERNAL)
    public SerializableMovementConfig(MovementMode flightMode, SwimMode swimMode, WalkMode walkMode, TriState sinksInWater, TriState flopsOnLand, boolean phasesThroughWalls, float gravity, float fallSpeedModifier, float walkSpeedModifier, float inertia) {
        this.flightMode = flightMode;
        this.swimMode = swimMode;
        this.walkMode = walkMode;
        this.sinksInWater = sinksInWater;
        this.flopsOnLand = flopsOnLand;
        this.phasesThroughWalls = phasesThroughWalls;
        this.gravity = gravity;
        this.fallSpeedModifier = fallSpeedModifier;
        this.walkSpeedModifier = walkSpeedModifier;
        this.inertia = inertia;
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeEnumConstant(this.flightMode);
        buf.writeEnumConstant(this.swimMode);
        buf.writeEnumConstant(this.walkMode);
        buf.writeEnumConstant(this.sinksInWater);
        buf.writeEnumConstant(this.flopsOnLand);
        buf.writeBoolean(this.climbsWalls);
        buf.writeBoolean(this.phasesThroughWalls);
        buf.writeFloat(this.gravity);
        buf.writeFloat(this.fallSpeedModifier);
        buf.writeFloat(this.inertia);
    }

    public void fromPacket(PacketByteBuf buf) {
        this.flightMode = buf.readEnumConstant(MovementMode.class);
        this.swimMode = buf.readEnumConstant(SwimMode.class);
        this.walkMode = buf.readEnumConstant(WalkMode.class);
        this.sinksInWater = buf.readEnumConstant(TriState.class);
        this.flopsOnLand = buf.readEnumConstant(TriState.class);
        this.climbsWalls = buf.readBoolean();
        this.phasesThroughWalls = buf.readBoolean();
        this.gravity = buf.readFloat();
        this.fallSpeedModifier = buf.readFloat();
        this.inertia = buf.readFloat();
    }

    @Override
    public MovementMode getFlightMode() {
        return flightMode;
    }

    @Override
    public float getAddedGravity() {
        return gravity;
    }

    @Override
    public float getFallSpeedModifier() {
        return fallSpeedModifier;
    }

    @Override
    public float getWalkSpeedModifier() {
        return walkSpeedModifier;
    }

    @Override
    public float getInertia() {
        return inertia;
    }

    @Override
    public SwimMode getSwimMode() {
        return swimMode;
    }

    @Override
    public WalkMode getWalkMode() {
        return walkMode;
    }

    @Override
    public TriState shouldSinkInWater() {
        return sinksInWater;
    }

    @Override
    public TriState shouldFlopOnLand() {
        return flopsOnLand;
    }

    @Override
    public boolean canClimbWalls() {
        return this.climbsWalls;
    }

    @Override
    public boolean canPhaseThroughWalls() {
        return this.phasesThroughWalls;
    }
}
