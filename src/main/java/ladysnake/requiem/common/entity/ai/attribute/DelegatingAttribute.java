/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.common.entity.ai.attribute;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.*;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public class DelegatingAttribute extends EntityAttributeInstanceImpl {
    private final EntityAttributeInstance original;

    public DelegatingAttribute(AbstractEntityAttributeContainer map, EntityAttributeInstance original) {
        super(map, original.getAttribute());
        this.original = original;
    }

    public final EntityAttributeInstance getOriginal() {
        return this.original;
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
    public Set<EntityAttributeModifier> getModifiers(EntityAttributeModifier.Operation operation) {
        return getDelegateAttributeInstance().getModifiers(operation);
    }

    @Override
    public Set<EntityAttributeModifier> getModifiers() {
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
