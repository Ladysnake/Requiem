package ladysnake.requiem.common.advancement.criterion;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.mixin.advancement.criterion.CriterionsAccessor;
import net.minecraft.advancement.criterion.Criterions;

public class RequiemCriteria {
    public static final OnResurrectCriterion PLAYER_RESURRECTED_AS_ENTITY = new OnResurrectCriterion(Requiem.id("player_resurrected_as_entity"));

    public static void init() {
        Criterions.getAllCriterions();
        CriterionsAccessor.invokeRegister(PLAYER_RESURRECTED_AS_ENTITY);
    }
}
