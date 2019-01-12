package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.possession.PossessionRegistry;
import ladysnake.dissolution.api.possession.Possessor;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.ActionResult;

public class Possession {
    private static PossessionRegistry registry = new SimplePossessionRegistry();

    public static PossessionRegistry getConversionRegistry() {
        return registry;
    }

    public static void init() {
        PlayerInteractionEvent.INTERACT_ENTITY_POSITIONED.register((player, world, hand, entity, hitPosition) -> {
            if (entity instanceof MobEntity && !entity.world.isClient) {
                MobEntity mob = (MobEntity) entity;
                if (((Possessor)player).startPossessing(mob)) {
                    return ActionResult.SUCCESS;
                }
            } else if (entity.world.isClient) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAILURE;
        });
    }
}
