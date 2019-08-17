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
package ladysnake.requiem.common.impl.resurrection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.util.EntityTypeAdapter;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ResurrectionDataLoader implements SimpleResourceReloadListener<List<ResurrectionData>> {
    public static final ResurrectionDataLoader INSTANCE = new ResurrectionDataLoader();

    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(new TypeToken<EntityType<?>>() {}.getType(), new EntityTypeAdapter())
        .create();

    private final List<ResurrectionData> resurrectionData = new ArrayList<>();

    @Nullable
    public MobEntity getNextBody(ServerPlayerEntity player, DamageSource killingBlow) {
        for (ResurrectionData resurrectionDatum : resurrectionData) {
            if (resurrectionDatum.matches(player, killingBlow)) {
                Entity nextBody = resurrectionDatum.createEntity(player.world);
                if (nextBody instanceof MobEntity) {
                    return (MobEntity) nextBody;
                }
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<ResurrectionData>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            List<ResurrectionData> resurrectionData = new ArrayList<>();
            for (Identifier location : manager.findResources("requiem_resurrections", (res) -> res.endsWith(".json"))) {
                try (Resource res = manager.getResource(location); Reader in = new InputStreamReader(res.getInputStream())) {
                    resurrectionData.add(ResurrectionData.deserialize(GSON.fromJson(in, JsonObject.class)));
                } catch (IOException | JsonParseException e) {
                    Requiem.LOGGER.error("Could not read resurrection data from {}", location, e);
                }
            }
            Collections.sort(resurrectionData);
            return resurrectionData;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(List<ResurrectionData> resurrectionData, ResourceManager resourceManager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            this.resurrectionData.clear();
            this.resurrectionData.addAll(resurrectionData);
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return Requiem.id("resurrection");
    }
}
