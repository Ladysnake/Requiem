package ladysnake.dissolution.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class SoulStrengthModifiedEvent extends PlayerEvent {
    private boolean strongSoul;

    public SoulStrengthModifiedEvent(EntityPlayer player, boolean strongSoul) {
        super(player);
        this.strongSoul = strongSoul;
    }

    public boolean isPlayerBecomingRemnant() {
        return this.strongSoul;
    }

    public void setNewSoulStrength(boolean newSoulStrength) {
        this.strongSoul = newSoulStrength;
    }

}
