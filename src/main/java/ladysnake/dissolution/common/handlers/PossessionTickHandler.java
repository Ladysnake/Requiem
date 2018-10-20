package ladysnake.dissolution.common.handlers;

import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@EnhancedBusSubscriber
public class PossessionTickHandler {

    @SubscribeEvent
    public void onTickPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
        IPossessable possessed = playerCorp.getPossessed();
        World world = event.player.world;
        if (possessed != null) {
            possessed.updatePossessing();
            if (world.isRemote) {
                possessed.possessTickClient();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
        IPossessable possessed = playerCorp.getPossessed();
        World world = event.player.world;
        if (possessed != null && !world.isRemote) {
            // TODO save possessed entity to player data and remove from world
        }
    }
}
