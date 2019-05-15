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
package ladysnake.requiem.common.impl.movement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.common.util.EntityTypeAdapter;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MovementAltererManager implements SubDataManager<Map<EntityType<?>, SerializableMovementConfig>> {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(new TypeToken<EntityType<?>>() {}.getType(), new EntityTypeAdapter()).create();
    public static final Identifier LOCATION = Requiem.id("entity_mobility.json");
    private static final Type TYPE = new TypeToken<Map<EntityType<?>, SerializableMovementConfig>>() {}.getType();
    public static final Identifier LISTENER_ID = Requiem.id("movement_alterer");

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
                    try {
                        ret.putAll(GSON.fromJson(new InputStreamReader(resource.getInputStream()), TYPE));
                    } catch (JsonIOException | JsonSyntaxException e) {
                        Requiem.LOGGER.warn("Could not read movement config from JSON file", e);
                    }
                }
            } catch (IOException e) {
                Requiem.LOGGER.error("Could not read movement configs", e);
            }
            return ret;
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }

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
