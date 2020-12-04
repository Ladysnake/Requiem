package ladysnake.requiem.api.v1.entity.ability;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public interface DirectAbility<E extends LivingEntity, T extends Entity> extends MobAbility<E> {
    Identifier ABILITY_ICON = new Identifier("requiem", "textures/gui/ability_icon.png");

    /**
     * If the range is 0, the vanilla targeting system is used
     */
    double getRange();

    Class<T> getTargetType();

    boolean canTarget(T target);

    boolean trigger(T target);

    @CheckEnv(Env.CLIENT)
    default Identifier getIconTexture() {
        return ABILITY_ICON;
    }
}
