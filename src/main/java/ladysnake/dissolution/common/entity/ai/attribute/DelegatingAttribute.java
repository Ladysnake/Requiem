package ladysnake.dissolution.common.entity.ai.attribute;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

public class DelegatingAttribute extends EntityAttributeInstanceImpl {
    protected final EntityAttributeInstance original;

    public DelegatingAttribute(AbstractEntityAttributeContainer map, EntityAttributeInstance original) {
        super(map, original.getAttribute());
        this.original = original;
    }

    protected EntityAttributeInstance getDelegateAttributeInstance() {
        return this.original;
    }

    @Override
    public EntityAttribute getAttribute() {
        return original.getAttribute();
    }

    @Override
    public double getBaseValue() {
        return getDelegateAttributeInstance().getBaseValue();
    }

    @Override
    public void setBaseValue(double baseValue) {
        getDelegateAttributeInstance().setBaseValue(baseValue);
    }

    @Override
    public Collection<EntityAttributeModifier> method_6193(EntityAttributeModifier.Operation operation) {
        return getDelegateAttributeInstance().method_6193(operation);
    }

    @Override
    public Collection<EntityAttributeModifier> getModifiers() {
        return getDelegateAttributeInstance().getModifiers();
    }

    @Override
    public boolean hasModifier(EntityAttributeModifier modifier) {
        return getDelegateAttributeInstance().hasModifier(modifier);
    }

    @Override
    @Nullable
    public EntityAttributeModifier getModifier(UUID uuid) {
        return getDelegateAttributeInstance().getModifier(uuid);
    }

    @Override
    public void addModifier(EntityAttributeModifier modifier) {
        getDelegateAttributeInstance().addModifier(modifier);
    }

    @Override
    public void removeModifier(EntityAttributeModifier modifier) {
        getDelegateAttributeInstance().removeModifier(modifier);
    }

    @Override
    public void removeModifier(UUID p_188479_1_) {
        getDelegateAttributeInstance().removeModifier(p_188479_1_);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clearModifiers() {
        getDelegateAttributeInstance().clearModifiers();
    }

    @Override
    public double getValue() {
        return getDelegateAttributeInstance().getValue();
    }

}
