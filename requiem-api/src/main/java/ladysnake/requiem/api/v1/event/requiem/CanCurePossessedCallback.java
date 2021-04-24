package ladysnake.requiem.api.v1.event.requiem;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.LivingEntity;

@FunctionalInterface
public interface CanCurePossessedCallback {
    /**
     * Checks if a LivingEntity can be cured. This is checked on start of the possession, and the end.
     * If any callback returns {@link TriState#FALSE}, it is assumed the entity cannot be cured. Use {@link TriState#FALSE} sparingly.
     * If your callback returns {@link TriState#TRUE}, it is not guaranteed that it will cure. <tt>It also may cause a crash if the curing isn't handled with {@link}</tt>
     * @param body the potentially curable LivingEntity
    **/
    TriState canCurePossessed(LivingEntity body);

    Event<CanCurePossessedCallback> EVENT = EventFactory.createArrayBacked(CanCurePossessedCallback.class,
        callbacks -> (body) -> {
            TriState storedState = TriState.DEFAULT;
            for (CanCurePossessedCallback callback : callbacks) {
                TriState state = callback.canCurePossessed(body);
                switch (state) {
                    case FALSE:
                        return TriState.FALSE;
                    case TRUE:
                        storedState = TriState.TRUE;
                }
            }
            return storedState;
        });
}
