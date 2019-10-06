package ladysnake.pandemonium;

import ladysnake.pandemonium.common.enchantment.PandemoniumEnchantments;
import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.pandemonium.common.entity.effect.PandemoniumPotions;
import ladysnake.pandemonium.common.entity.effect.PandemoniumStatusEffects;
import ladysnake.pandemonium.common.network.ServerMessageHandling;
import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@CalledThroughReflection
public final class Pandemonium implements ModInitializer {
    public static final String MOD_ID = "pandemonium";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        PandemoniumEntities.init();
        PandemoniumEnchantments.init();
        PandemoniumPotions.init();
        PandemoniumStatusEffects.init();
        ServerMessageHandling.init();
        RequiemApi.registerPlugin(new PandemoniumRequiemPlugin());
    }
}
