package ladysnake.requiem.common.impl.resurrection;

import com.google.gson.JsonElement;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Objects;

public final class ExtendedDamageSourcePredicate {
    public static final ExtendedDamageSourcePredicate EMPTY = new ExtendedDamageSourcePredicate(DamageSourcePredicate.EMPTY, null);
    private final DamageSourcePredicate base;
    private final String damageName;

    private ExtendedDamageSourcePredicate(DamageSourcePredicate base, String damageName) {
        this.base = base;
        this.damageName = damageName;
    }

    public boolean test(ServerPlayerEntity player, DamageSource damage) {
        return (damageName == null || damageName.equals(damage.name)) || base.test(player, damage);
    }

    public static ExtendedDamageSourcePredicate deserialize(@Nullable JsonElement element) {
        DamageSourcePredicate base = DamageSourcePredicate.deserialize(element);
        if (base == DamageSourcePredicate.EMPTY) {
            return EMPTY;
        }
        String damageName = JsonHelper.getString(Objects.requireNonNull(element).getAsJsonObject(), "name", null);
        return new ExtendedDamageSourcePredicate(base, damageName);
    }
}
