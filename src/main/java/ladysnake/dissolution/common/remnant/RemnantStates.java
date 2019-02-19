package ladysnake.dissolution.common.remnant;

import ladysnake.dissolution.api.v1.remnant.RemnantType;
import ladysnake.dissolution.common.impl.remnant.NullRemnantState;

public final class RemnantStates {
    private RemnantStates() { throw new AssertionError(); }

    public static final RemnantType NULL = p -> NullRemnantState.NULL_STATE;
    public static final RemnantType LARVA = owner -> new FracturableRemnantState(RemnantStates.LARVA, owner);
    public static final RemnantType YOUNG = owner -> new AstralRemnantState(RemnantStates.YOUNG, owner);

}
