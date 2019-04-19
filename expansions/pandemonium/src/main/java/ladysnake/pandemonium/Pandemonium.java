package ladysnake.pandemonium;

import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.pandemonium.common.network.ServerMessageHandling;
import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

@CalledThroughReflection
public class Pandemonium implements ModInitializer {
    public static final String MOD_ID = "pandemonium";

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        PandemoniumEntities.init();
        ServerMessageHandling.init();
        RequiemApi.registerPlugin(new PandemoniumRequiemPlugin());
    }
}
