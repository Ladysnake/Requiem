package ladysnake.dissolution.common.impl.movement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.entity.MovementConfig;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MovementAltererManager implements IdentifiableResourceReloadListener<Void>, SynchronousResourceReloadListener {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(new TypeToken<EntityType<?>>() {}.getType(), new EntityTypeAdapter()).create();
    public static final Identifier LOCATION = Dissolution.id("entity_mobility.json");
    private static final Type TYPE = new TypeToken<Map<EntityType<?>, SerializableMovementConfig>>() {}.getType();
    public static final Identifier LISTENER_ID = Dissolution.id("movement_alterer");

    private final Map<EntityType<?>, SerializableMovementConfig> entityMovementConfigs = new HashMap<>();

    // TODO remove the unneeded override when those methods are mapped
    @Override
    public void apply(ResourceManager var1, Void var2, Profiler var3) {
        this.method_14491(var1);
    }

    @Override
    public void method_14491(ResourceManager dataManager) {
        entityMovementConfigs.clear();
        try {
            for (Resource resource : dataManager.getAllResources(LOCATION)) {
                entityMovementConfigs.putAll(GSON.fromJson(new InputStreamReader(resource.getInputStream()), TYPE));
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public MovementConfig getEntityMovementConfig(EntityType<?> type) {
        return this.entityMovementConfigs.getOrDefault(type, new SerializableMovementConfig());
    }

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }
}
