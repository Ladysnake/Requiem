package ladysnake.dissolution.api.corporeality;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerIncorporealEvent extends PlayerEvent {

    private final ICorporealityStatus newStatus;

    public PlayerIncorporealEvent(EntityPlayer player, ICorporealityStatus newStatus) {
        super(player);
        this.newStatus = newStatus;
    }

    public ICorporealityStatus getNewStatus() {
        return newStatus;
    }
}
