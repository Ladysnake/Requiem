package ladysnake.dissolution.api.corporeality;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

/**
 * Indicates that incorporeal players are able to interact with this block or entity
 */
public interface ISoulInteractable {

    /**
     * Only called if this is an entity
     * @see Entity#applyPlayerInteraction(EntityPlayer, Vec3d, EnumHand)
     */
    default EnumActionResult applySoulInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (this instanceof Entity) {
            return ((Entity) this).applyPlayerInteraction(player, vec, hand);
        }
        return EnumActionResult.PASS;
    }
}
