package ladysnake.dissolution.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SoulStrengthModifiedEvent extends PlayerEvent {
    private boolean strongSoul;

    public SoulStrengthModifiedEvent(EntityPlayer player, boolean strongSoul) {
        super(player);
        this.strongSoul = strongSoul;
    }

    /**
     *
     * @return true if
     */
    public boolean isChangedToStrong() {
        return this.strongSoul;
    }

    public void setNewSoulStrength(boolean newSoulStrength) {
        this.strongSoul = newSoulStrength;
    }

}
