package ladysnake.requiem.api.v1.util;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.entity.ai.TargetPredicate;

/**
 * Interface implemented by {@link net.minecraft.entity.ai.TargetPredicate} when Requiem is installed.
 */
public interface RequiemTargetPredicate {
    static TargetPredicate includeIncorporeal(TargetPredicate predicate) {
        ((RequiemTargetPredicate) predicate).requiem$includeIncorporeal();
        return predicate;
    }

    /**
     * Causes this target predicate to include players that are {@link RemnantComponent#isIncorporeal() incorporeal}.
     */
    void requiem$includeIncorporeal();
}
