package ladysnake.pandemonium.compat;

import ladysnake.pandemonium.api.event.PlayerShellEvents;
import ladysnake.requiem.compat.OriginHolder;

public final class PandemoniumOriginsCompat {
    public static void init() {
        PlayerShellEvents.PLAYER_SPLIT.register((whole, soul, playerShell, playerData) -> {
            OriginHolder.KEY.get(playerShell).storeOrigin(whole);
        });

        PlayerShellEvents.PLAYER_MERGED.register((player, playerShell, shellProfile, playerData) -> {
            // First, store a backup of the player's actual origin
            OriginHolder.KEY.get(player).storeOrigin(player);
            // Then, give the player the shell's origin
            OriginHolder.KEY.get(playerShell).restoreOrigin(player);
        });
    }
}
