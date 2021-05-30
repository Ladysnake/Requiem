package ladysnake.pandemonium.common.entity.internal;

import ladysnake.requiem.common.util.reflection.ReflectionHelper;
import ladysnake.requiem.common.util.reflection.UnableToFindMethodException;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.world.World;

import java.lang.reflect.Method;

public abstract class SpellcastingIllagerAccess extends SpellcastingIllagerEntity {
    public static final SpellcastingIllagerEntity.Spell SPELL_NONE = Spell.NONE;
    public static final Spell SPELL_FANGS = Spell.FANGS;
    public static final Method CAST_SPELL_GOAL$CAST_SPELL;

    static {
        try {
            CAST_SPELL_GOAL$CAST_SPELL = ReflectionHelper.findMethodFromIntermediary(CastSpellGoal.class, "method_7148", void.class);
        } catch (UnableToFindMethodException e) {
            throw new UncheckedReflectionException(e);
        }
    }

    private SpellcastingIllagerAccess(EntityType<? extends SpellcastingIllagerEntity> entityType, World world) {
        super(entityType, world);
    }
}
