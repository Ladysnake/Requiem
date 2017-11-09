package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.IIncorporealHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PlayerIncorporealEvent extends PlayerEvent {

    private final IIncorporealHandler.CorporealityStatus newStatus;

    public PlayerIncorporealEvent(EntityPlayer player, IIncorporealHandler.CorporealityStatus newStatus) {
        super(player);
        this.newStatus = newStatus;
    }

    public IIncorporealHandler.CorporealityStatus getNewStatus() {
        return newStatus;
    }
}
