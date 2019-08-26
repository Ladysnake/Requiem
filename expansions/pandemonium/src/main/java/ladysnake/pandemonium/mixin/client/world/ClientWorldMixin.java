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
package ladysnake.pandemonium.mixin.client.world;

import ladysnake.pandemonium.client.ClientAnchorManager;
import ladysnake.pandemonium.api.PandemoniumWorld;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World implements PandemoniumWorld {
    private final FractureAnchorManager anchorManager = new ClientAnchorManager(this);

    protected ClientWorldMixin(LevelProperties props, DimensionType dim, BiFunction<World, Dimension, ChunkManager> biFunction_1, Profiler profiler_1, boolean boolean_1) {
        super(props, dim, biFunction_1, profiler_1, boolean_1);
    }

    @Override
    public FractureAnchorManager getAnchorManager() {
        return this.anchorManager;
    }

    @Inject(method = "tickEntities", at = @At("TAIL"))
    private void updateAnchorTracker(CallbackInfo ci) {
        Profiler profiler = this.getProfiler();
        profiler.push("requiem_ethereal_anchors");
        this.getAnchorManager().updateAnchors(this.properties.getTime());
        profiler.pop();
    }
}
