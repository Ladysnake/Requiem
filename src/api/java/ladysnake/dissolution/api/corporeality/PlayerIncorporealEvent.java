package ladysnake.dissolution.api.corporeality;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerIncorporealEvent extends PlayerEvent {

    private final ICorporealityStatus newStatus;
    private final boolean forced;

    public PlayerIncorporealEvent(EntityPlayer player, ICorporealityStatus newStatus, boolean forced) {
        super(player);
        this.newStatus = newStatus;
        this.forced = forced;
    }

    public boolean isForced() {
        return forced;
    }

    @Override
    public boolean isCancelable() {
        return !isForced();
    }

    public ICorporealityStatus getNewStatus() {
        return newStatus;
    }
}
