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
package ladysnake.requiem.core.movement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import ladysnake.requiem.api.v1.entity.MovementRegistry;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.util.serde.EntityTypeAdapter;
import ladysnake.requiem.core.util.serde.TriStateTypeAdapter;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class MovementAltererManager implements SubDataManager<Map<EntityType<?>, SerializableMovementConfig>>, MovementRegistry {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        // Replacing this TypeToken with a regular class will burn gson to the ground
        .registerTypeAdapter(new TypeToken<EntityType<?>>() {}.getType(), new EntityTypeAdapter())
        .registerTypeAdapter(TriState.class, new TriStateTypeAdapter())
        .create();
    public static final Identifier LOCATION = RequiemCore.id("entity_mobility.json");
    public static final Identifier LISTENER_ID = RequiemCore.id("movement_alterer");

    private final Map<EntityType<?>, SerializableMovementConfig> entityMovementConfigs = new HashMap<>();

    @Override
    public void apply(Map<EntityType<?>, SerializableMovementConfig> data) {
        entityMovementConfigs.clear();
        entityMovementConfigs.putAll(data);
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        buf.writeVarInt(entityMovementConfigs.size());
        for (Map.Entry<EntityType<?>, SerializableMovementConfig> entry : entityMovementConfigs.entrySet()) {
            buf.writeIdentifier(EntityType.getId(entry.getKey()));
            entry.getValue().toPacket(buf);
        }
    }

    @Override
    public Map<EntityType<?>, SerializableMovementConfig> loadFromPacket(PacketByteBuf buf) {
        Map<EntityType<?>, SerializableMovementConfig> ret = new HashMap<>();
        int nbConfigs = buf.readVarInt();
        for (int i = 0; i < nbConfigs; i++) {
            Identifier id = buf.readIdentifier();
            SerializableMovementConfig conf = new SerializableMovementConfig();
            conf.fromPacket(buf);
            ret.put(Registry.ENTITY_TYPE.get(id), conf);
        }
        return ret;
    }

    @Override
    public CompletableFuture<Map<EntityType<?>, SerializableMovementConfig>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<EntityType<?>, SerializableMovementConfig> ret = new HashMap<>();
            try {
                for (Resource resource : manager.getAllResources(LOCATION)) {
                    try (InputStreamReader in = new InputStreamReader(resource.getInputStream())) {
                        JsonObject map = JsonHelper.deserialize(in);
                        for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                            Optional<EntityType<?>> type = EntityType.get(entry.getKey());
                            if (type.isPresent()) {
                                ret.put(type.get(), GSON.fromJson(entry.getValue(), SerializableMovementConfig.class));
                            } else if (JsonHelper.getBoolean(entry.getValue().getAsJsonObject(), "required", true)) {
                                throw new JsonSyntaxException("Not a valid entity type: " + entry.getKey());
                            } // else continue;
                        }
                    } catch (JsonIOException | JsonSyntaxException e) {
                        RequiemCore.LOGGER.warn("Could not read movement config from JSON file", e);
                    }
                }
            } catch (IOException e) {
                RequiemCore.LOGGER.error("Could not read movement configs", e);
            }
            return ret;
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }

    @Override
    public MovementConfig getEntityMovementConfig(EntityType<?> type) {
        return this.entityMovementConfigs.getOrDefault(type, new SerializableMovementConfig());
    }

    @Override
    public String toString() {
        return "MovementAltererManager{" +
                "entityMovementConfigs=" + entityMovementConfigs.keySet() +
                '}';
    }
}
