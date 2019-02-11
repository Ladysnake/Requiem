package ladysnake.satin.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.entity.Entity;

@FunctionalInterface
public interface PreBlockEntitiesCallback {
    /**
     * Fired after Minecraft has rendered all entities and before it renders block entities.
     */
    Event<PreBlockEntitiesCallback> EVENT = EventFactory.createArrayBacked(PreBlockEntitiesCallback.class,
            (listeners) -> (Entity camera, VisibleRegion frustum, float tickDelta) -> {
                for (PreBlockEntitiesCallback handler : listeners) {
                    handler.onPreRenderBlockEntities(camera, frustum, tickDelta);
                }
            });

    void onPreRenderBlockEntities(Entity camera, VisibleRegion frustum, float tickDelta);
}
