package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.v1.possession.Possessable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import org.apiguardian.api.API;

import java.util.function.BiConsumer;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

public final class CopyStrategies {
    private CopyStrategies() { throw new AssertionError(); }

    @API(status = EXPERIMENTAL)
    public static <E extends MobEntity, P extends MobEntity & Possessable> BiConsumer<E, P> basicCopy() {
        return (entity, clone) -> {
            clone.setPositionAndAngles(entity);
            clone.setEntityId(entity.getEntityId());
            clone.setUuid(entity.getUuid());
        };
    }

    @API(status = EXPERIMENTAL)
    public static <E extends MobEntity, P extends MobEntity & Possessable> BiConsumer<E, P> nbtCopy() {
        return (entity, clone) -> {
            CompoundTag serialized = new CompoundTag();
            entity.toTag(serialized);
            clone.fromTag(serialized);
            clone.setEntityId(entity.getEntityId());
        };
    }
}
