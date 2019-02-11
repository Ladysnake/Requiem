package ladysnake.dissolution.api.v1.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

@FunctionalInterface
public interface ItemPickupCallback {

    ActionResult onItemPickup(PlayerEntity player, ItemEntity pickedUp);

    Event<ItemPickupCallback> EVENT = EventFactory.createArrayBacked(ItemPickupCallback.class,
            (listeners) -> (player, pickedUp) -> {
                for (ItemPickupCallback handler : listeners) {
                    ActionResult actionResult = handler.onItemPickup(player, pickedUp);
                    if (actionResult != ActionResult.PASS) {
                        return actionResult;
                    }
                }
                return ActionResult.PASS;
            });
}
