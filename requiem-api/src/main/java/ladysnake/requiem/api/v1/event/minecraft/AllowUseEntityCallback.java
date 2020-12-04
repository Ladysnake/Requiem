package ladysnake.requiem.api.v1.event.minecraft;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@FunctionalInterface
public interface AllowUseEntityCallback {
    Event<AllowUseEntityCallback> EVENT = EventFactory.createArrayBacked(AllowUseEntityCallback.class,
        (listeners) -> (player, world, hand, entity) -> {
            for (AllowUseEntityCallback event : listeners) {

                if (!event.allow(player, world, hand, entity)) {
                    return false;
                }
            }

            return true;
        }
    );

    boolean allow(PlayerEntity player, World world, Hand hand, Entity entity);
}
