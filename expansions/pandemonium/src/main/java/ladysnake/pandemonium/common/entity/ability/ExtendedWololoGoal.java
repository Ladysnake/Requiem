package ladysnake.pandemonium.common.entity.ability;

import net.minecraft.entity.ai.TargetPredicate;

public interface ExtendedWololoGoal {
    default TargetPredicate requiem_getConvertibleSheepPredicate() {
        throw new AssertionError();
    }

    default boolean requiem_hasValidTarget() {
        return false;
    }
}
