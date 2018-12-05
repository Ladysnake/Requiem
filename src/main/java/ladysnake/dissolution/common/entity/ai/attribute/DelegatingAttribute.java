package ladysnake.dissolution.common.entity.ai.attribute;

import net.minecraft.entity.ai.attributes.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

public class DelegatingAttribute extends ModifiableAttributeInstance {
    protected IAttributeInstance original;

    public DelegatingAttribute(AbstractAttributeMap map, IAttributeInstance original) {
        super(map, original.getAttribute());
        this.original = original;
    }

    protected IAttributeInstance getDelegateAttributeInstance() {
        return this.original;
    }

    @Override
    public IAttribute getAttribute() {
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
    public Collection<AttributeModifier> getModifiersByOperation(int operation) {
        return getDelegateAttributeInstance().getModifiersByOperation(operation);
    }

    @Override
    public Collection<AttributeModifier> getModifiers() {
        return getDelegateAttributeInstance().getModifiers();
    }

    @Override
    public boolean hasModifier(AttributeModifier modifier) {
        return getDelegateAttributeInstance().hasModifier(modifier);
    }

    @Override
    @Nullable
    public AttributeModifier getModifier(UUID uuid) {
        return getDelegateAttributeInstance().getModifier(uuid);
    }

    @Override
    public void applyModifier(AttributeModifier modifier) {
        getDelegateAttributeInstance().applyModifier(modifier);
    }

    @Override
    public void removeModifier(AttributeModifier modifier) {
        getDelegateAttributeInstance().removeModifier(modifier);
    }

    @Override
    public void removeModifier(UUID p_188479_1_) {
        getDelegateAttributeInstance().removeModifier(p_188479_1_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void removeAllModifiers() {
        getDelegateAttributeInstance().removeAllModifiers();
    }

    @Override
    public double getAttributeValue() {
        return getDelegateAttributeInstance().getAttributeValue();
    }

}
