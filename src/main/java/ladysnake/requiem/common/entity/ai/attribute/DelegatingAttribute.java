/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
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
