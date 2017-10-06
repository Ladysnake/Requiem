package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.IIncorporealHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PlayerIncorporealEvent extends Event {

    private final IIncorporealHandler.CorporealityStatus newStatus;
    private final EntityPlayer player;

    public PlayerIncorporealEvent(EntityPlayer player, IIncorporealHandler.CorporealityStatus newStatus) {
        this.newStatus = newStatus;
        this.player = player;
    }

    public IIncorporealHandler.CorporealityStatus getNewStatus() {
        return newStatus;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
