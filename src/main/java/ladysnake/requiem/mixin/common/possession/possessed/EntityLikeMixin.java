package ladysnake.requiem.mixin.common.possession.possessed;

import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(EntityLike.class)
public interface EntityLikeMixin extends ProtoPossessable {
    @Nullable
    @Override
    default PlayerEntity getPossessor() {
        return null;
    }

    @Override
    default boolean isBeingPossessed() {
        return false;
    }
}
