package ladysnake.requiem.api.v1.event.minecraft.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface UpdateTargetedEntityCallback {
    void updateTargetedEntity(float tickDelta);

    Event<UpdateTargetedEntityCallback> EVENT = EventFactory.createArrayBacked(UpdateTargetedEntityCallback.class,
        (listeners) -> (tickDelta) -> {
            for (UpdateTargetedEntityCallback handler : listeners) {
                handler.updateTargetedEntity(tickDelta);
            }
        });
}
