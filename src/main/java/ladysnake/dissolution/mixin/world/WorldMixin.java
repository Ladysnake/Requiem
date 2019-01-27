package ladysnake.dissolution.mixin.world;

import ladysnake.dissolution.api.v1.remnant.RemnantState;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(World.class)
public abstract class WorldMixin {

    private static final Predicate<Entity> EXCEPT_JUST_SPECTATOR = EntityPredicates.EXCEPT_SPECTATOR.or(RemnantState.REMNANT);

    /**
     * @see ladysnake.dissolution.mixin.predicate.entity.EntityPredicatesMixin
     */
    @SuppressWarnings("InvalidMemberReference") // Arrays of methods are valid
    @Redirect(
            method = {"findVisiblePlayer", "findClosestVisiblePlayer"},
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z",
                    ordinal = 0
            )
    )
    private boolean makeSoulsVisibleAgain(Predicate<Entity> self, Object tested) {
        if (self != EntityPredicates.EXCEPT_SPECTATOR) {
            throw new IllegalStateException("[Dissolution] World#find(Closest)VisiblePlayer is not using the expected predicate EXCEPT_SPECTATOR !");
        }
        return EXCEPT_JUST_SPECTATOR.test((Entity) tested);
    }
}
