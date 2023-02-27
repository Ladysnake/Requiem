package ladysnake.requiem.mixin.common.possession.gameplay;

import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.BatEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * bats stop roosting whenever there is a player next to them - and we don't want that to happen with ghosts
 */
@Mixin(BatEntity.class)
public abstract class BatEntityMixin implements Possessable {
    @Shadow
    @Final
    private static TargetPredicate CLOSE_PLAYER_PREDICATE;

    static {
        CLOSE_PLAYER_PREDICATE.setPredicate(p -> !RemnantComponent.isVagrant(p));
    }
}
