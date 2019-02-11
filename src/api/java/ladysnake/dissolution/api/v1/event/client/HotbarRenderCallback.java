package ladysnake.dissolution.api.v1.event.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface HotbarRenderCallback {

    ActionResult onHotbarRendered(float tickDelta);

    Event<HotbarRenderCallback> EVENT = EventFactory.createArrayBacked(HotbarRenderCallback.class,
            (listeners) -> (tickDelta) -> {
                for (HotbarRenderCallback handler : listeners) {
                    ActionResult actionResult = handler.onHotbarRendered(tickDelta);
                    if (actionResult != ActionResult.PASS) {
                        return actionResult;
                    }
                }
                return ActionResult.PASS;
            });

}
