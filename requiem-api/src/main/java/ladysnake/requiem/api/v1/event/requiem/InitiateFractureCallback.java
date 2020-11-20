package ladysnake.requiem.api.v1.event.requiem;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface InitiateFractureCallback {
    boolean performFracture(ServerPlayerEntity player);

    Event<InitiateFractureCallback> EVENT = EventFactory.createArrayBacked(InitiateFractureCallback.class,
        callbacks -> player -> {
            for (InitiateFractureCallback callback : callbacks) {
                if (callback.performFracture(player)) {
                    return true;
                }
            }
            return false;
        });
}
