package ladysnake.dissolution.lib;

import net.fabricmc.loader.launch.common.FabricLauncherBase;

public final class DebugUtil {
    private DebugUtil() { throw new AssertionError(); }

    public static boolean isDevEnv() {
        return FabricLauncherBase.getLauncher().isDevelopment();
    }
}
