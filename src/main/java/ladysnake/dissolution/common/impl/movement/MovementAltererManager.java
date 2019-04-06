package ladysnake.dissolution.common.impl.movement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.entity.MovementConfig;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MovementAltererManager implements SimpleResourceReloadListener<Map<EntityType<?>, SerializableMovementConfig>> {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(new TypeToken<EntityType<?>>() {}.getType(), new EntityTypeAdapter()).create();
    public static final Identifier LOCATION = Dissolution.id("entity_mobility.json");
    private static final Type TYPE = new TypeToken<Map<EntityType<?>, SerializableMovementConfig>>() {}.getType();
    public static final Identifier LISTENER_ID = Dissolution.id("movement_alterer");

    private final Map<EntityType<?>, SerializableMovementConfig> entityMovementConfigs = new HashMap<>();

    @Override
    public CompletableFuture<Void> apply(Map<EntityType<?>, SerializableMovementConfig> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            entityMovementConfigs.clear();
            entityMovementConfigs.putAll(data);
        }, executor);
    }

    @Override
    public CompletableFuture<Void> reload(Helper var1, ResourceManager var2, Profiler var3, Profiler var4, Executor var5, Executor var6) {
        return apply(var1, var2, var3, var4, var5, var6);
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
                        Dissolution.LOGGER.warn("Could not read movement config from JSON file", e);
                    }
                }
            } catch (IOException e) {
                Dissolution.LOGGER.error("Could not read movement configs", e);
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
}
