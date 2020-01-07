package ladysnake.requiem.common.util;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.requiem.HumanityCheckCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import org.jetbrains.annotations.Nullable;

public final class DamageHelper {
    public static int getHumanityLevel(DamageSource source) {
        LivingEntity attacker = getPossessionAttacker(source);
        if (attacker != null) {
            return HumanityCheckCallback.EVENT.invoker().getHumanityLevel(attacker);
        }
        return 0;
    }

    /**
     * Returns {@code true} if {@code attacker} is a possessed item user, or a possessing player
     */
    @Nullable
    private static LivingEntity getPossessionAttacker(DamageSource source) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof Possessable && ((Possessable) attacker).isBeingPossessed() && RequiemEntityTypeTags.ITEM_USER.contains(attacker.getType())) {
            return (LivingEntity) attacker;
        }
        if (attacker instanceof RequiemPlayer) {
            return ((RequiemPlayer) attacker).asPossessor().getPossessedEntity();
        }
        return null;
    }

    public static DamageSource tryProxyDamage(DamageSource source, LivingEntity attacker) {
        Entity delegate = getDamageDelegate(attacker);
        if (delegate != null) {
            return createProxiedDamage(source, delegate);
        }
        return null;
    }

    @Nullable
    private static Entity getDamageDelegate(LivingEntity attacker) {
        if (attacker instanceof RequiemPlayer) {
            return ((RequiemPlayer) attacker).asPossessor().getPossessedEntity();
        }
        return null;
    }

    @Nullable
    public static DamageSource createProxiedDamage(DamageSource source, Entity newAttacker) {
        if (source instanceof ProjectileDamageSource) {
            return new ProjectileDamageSource(source.getName(), source.getSource(), newAttacker);
        } else if (source instanceof EntityDamageSource) {
            return new EntityDamageSource(source.getName(), newAttacker);
        }
        return null;
    }
}
