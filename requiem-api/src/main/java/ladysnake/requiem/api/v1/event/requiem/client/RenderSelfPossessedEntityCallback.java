package ladysnake.requiem.api.v1.event.requiem.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

public interface RenderSelfPossessedEntityCallback {
    boolean allowRender(Entity possessed);

    Event<RenderSelfPossessedEntityCallback> EVENT = EventFactory.createArrayBacked(RenderSelfPossessedEntityCallback.class,
        (listeners) -> (possessed) -> {
            for (RenderSelfPossessedEntityCallback handler : listeners) {
                if (handler.allowRender(possessed)) {
                    return true;
                }
            }
            return false;
        });
}
