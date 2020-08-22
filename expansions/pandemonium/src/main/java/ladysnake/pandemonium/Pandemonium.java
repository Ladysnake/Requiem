package ladysnake.pandemonium;

import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import ladysnake.pandemonium.client.ClientAnchorManager;
import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.pandemonium.common.impl.anchor.CommonAnchorManager;
import ladysnake.pandemonium.common.network.ServerMessageHandling;
import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

@CalledThroughReflection
public class Pandemonium implements ModInitializer, WorldComponentInitializer {
    public static final String MOD_ID = "pandemonium";

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    private static void tickAnchors(World world) {
        Profiler profiler = world.getProfiler();
        profiler.push("requiem_ethereal_anchors");
        FractureAnchorManager.get(world).updateAnchors(world.getLevelProperties().getTime());
        profiler.pop();
    }

    @Override
    public void onInitialize() {
        PandemoniumEntities.init();
        ServerMessageHandling.init();
        RequiemApi.registerPlugin(new PandemoniumRequiemPlugin());
        ServerTickEvents.END_WORLD_TICK.register(Pandemonium::tickAnchors);
        ClientTickEvents.END_WORLD_TICK.register(Pandemonium::tickAnchors);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(FractureAnchorManager.COMPONENT, world -> world.isClient
            ? new ClientAnchorManager(world)
            : new CommonAnchorManager(world)
        );
    }
}
