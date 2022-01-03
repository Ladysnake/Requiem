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
package ladysnake.requiem.core.resurrection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.util.serde.EntityTypeAdapter;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ResurrectionDataLoader implements SimpleResourceReloadListener<List<Pair<Identifier, JsonObject>>> {
    public static final ResurrectionDataLoader INSTANCE = new ResurrectionDataLoader();

    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(new TypeToken<EntityType<?>>() {}.getType(), new EntityTypeAdapter())
        .create();

    private final List<ResurrectionData> resurrectionData = new ArrayList<>();

    @Nullable
    public MobEntity getNextBody(ServerPlayerEntity player, DamageSource killingBlow) {
        return getNextBody(player, null, killingBlow);
    }

    @Nullable
    public MobEntity getNextBody(ServerPlayerEntity player, @Nullable LivingEntity possessed, DamageSource killingBlow) {
        for (ResurrectionData resurrectionDatum : resurrectionData) {
            if (resurrectionDatum.matches(player, possessed, killingBlow)) {
                Entity nextBody = resurrectionDatum.createEntity(player.world);
                if (nextBody instanceof MobEntity) {
                    nextBody.copyPositionAndRotation(player);
                    return (MobEntity) nextBody;
                }
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<Pair<Identifier, JsonObject>>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            List<Pair<Identifier, JsonObject>> resurrectionData = new ArrayList<>();
            for (Identifier location : manager.findResources("requiem_resurrections", (res) -> res.endsWith(".json"))) {
                try (Resource res = manager.getResource(location); Reader in = new InputStreamReader(res.getInputStream())) {
                    resurrectionData.add(new Pair<>(location, GSON.fromJson(in, JsonObject.class)));
                } catch (IOException | JsonParseException e) {
                    RequiemCore.LOGGER.error("[Requiem] Could not read resurrection data from {}", location, e);
                }
            }
            return resurrectionData;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(List<Pair<Identifier, JsonObject>> resurrectionData, ResourceManager resourceManager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            this.resurrectionData.clear();
            for (Pair<Identifier, JsonObject> resurrectionDatum : resurrectionData) {
                try {
                    this.resurrectionData.add(ResurrectionData.deserialize(resurrectionDatum.getRight()));
                } catch (JsonParseException e) {
                    RequiemCore.LOGGER.error("[Requiem] Could not read resurrection data from {}", resurrectionDatum.getLeft(), e);
                }
            }
            Collections.sort(this.resurrectionData);
        }, executor);
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(ResourceReloadListenerKeys.TAGS);
    }

    @Override
    public Identifier getFabricId() {
        return RequiemCore.id("resurrection");
    }
}
