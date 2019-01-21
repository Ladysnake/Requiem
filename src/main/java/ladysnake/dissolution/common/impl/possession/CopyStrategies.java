package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.v1.possession.Possessable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

public final class CopyStrategies {
    private CopyStrategies() { throw new AssertionError(); }

    /**
     * Intended to be used as a method reference
     */
    @API(status = EXPERIMENTAL)
    public static <E extends MobEntity, P extends MobEntity & Possessable> void basicCopy(E entity, P clone) {
        clone.setPositionAndAngles(entity);
        clone.setEntityId(entity.getEntityId());
        clone.setUuid(entity.getUuid());
    }

    /**
     * Intended to be used as a method reference
     */
    @API(status = EXPERIMENTAL)
    public static <E extends MobEntity, P extends MobEntity & Possessable> void nbtCopy(E entity, P clone) {
        CompoundTag serialized = new CompoundTag();
        entity.toTag(serialized);
        clone.fromTag(serialized);
        clone.setEntityId(entity.getEntityId());
    }
}
