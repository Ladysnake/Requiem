package ladysnake.requiem.compat;

import ladysnake.requiem.Requiem;
import net.fabricmc.loader.api.FabricLoader;

public final class RequiemCompatibilityManager {
    public static void init() {
        try {
            load("eldritch_mobs", EldritchMobsCompat::init);
        } catch (Throwable t) {
            Requiem.LOGGER.error("[Requiem] Failed to load compatibility hooks", t);
        }
    }

    private static void load(String modId, Runnable action) {
        try {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                action.run();
            }
        } catch (Throwable t) {
            Requiem.LOGGER.error("[Requiem] Failed to load compatibility hooks for {}", modId, t);
        }
    }
}
