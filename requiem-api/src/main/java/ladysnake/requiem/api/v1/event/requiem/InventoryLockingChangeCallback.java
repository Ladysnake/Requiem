package ladysnake.requiem.api.v1.event.requiem;

import ladysnake.requiem.api.v1.entity.InventoryPart;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

@FunctionalInterface
public interface InventoryLockingChangeCallback {
    void onInventoryLockingChange(PlayerEntity player, InventoryPart part, boolean locked);

    Event<InventoryLockingChangeCallback> EVENT = EventFactory.createArrayBacked(InventoryLockingChangeCallback.class,
        callbacks -> (player, part, locked) -> {
            for (InventoryLockingChangeCallback callback : callbacks) {
                callback.onInventoryLockingChange(player, part, locked);
            }
        });
}
