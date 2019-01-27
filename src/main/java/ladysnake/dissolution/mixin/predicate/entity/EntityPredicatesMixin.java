package ladysnake.dissolution.mixin.predicate.entity;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EntityPredicates.class)
public abstract class EntityPredicatesMixin {
    @Shadow @Mutable @Final public static Predicate<Entity> EXCEPT_SPECTATOR;

    /**
     * We want to prevent collisions with / selection of soul players.
     * The easiest way to do this is to make them be considered spectators.
     * (Just considering vanilla projectiles, there are 3 ways to get collided entities,
     * all using this predicate at some point)
     * Issue: we have to patch some methods in world to actually ignore it.
     * Suggestions are welcome.
     * @see ladysnake.dissolution.mixin.world.WorldMixin
     */
    @SuppressWarnings("UnresolvedMixinReference")   // <clinit> is in fact a valid target
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void excludeSouls(CallbackInfo info) {
        EXCEPT_SPECTATOR = EXCEPT_SPECTATOR.and(e -> !(e instanceof DissolutionPlayer) || !((DissolutionPlayer)e).getRemnantState().isSoul());
    }
}
