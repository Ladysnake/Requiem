package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.possession.PossessableSubstitutionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

import javax.annotation.Nullable;

public interface PossessableConverterProvider {
    @Nullable
    <T extends MobEntity> PossessableSubstitutionHandler<T> get(EntityType<T> type);
}
