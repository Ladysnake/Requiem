package ladysnake.pandemonium.compat;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.compat.OriginHolder;
import ladysnake.requiem.compat.RequiemCompatibilityManager;
import net.fabricmc.loader.api.FabricLoader;

public final class PandemoniumCompatibilityManager {
    public static void init() {
        try {
            RequiemCompatibilityManager.load("origins", PandemoniumOriginsCompat::init);
        } catch (Throwable t) {
            Requiem.LOGGER.error("[Pandemonium] Failed to load compatibility hooks", t);
        }
    }

    public static void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        if (FabricLoader.getInstance().isModLoaded("origins")) {
            registry.registerFor(PlayerShellEntity.class, OriginHolder.KEY, shell -> new OriginHolder());
        }
    }
}
