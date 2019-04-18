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

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;

public class PossessionDelegatingAttribute extends DelegatingAttribute {
    private final PossessionComponent handler;

    public PossessionDelegatingAttribute(AbstractEntityAttributeContainer map, EntityAttributeInstance original, PossessionComponent handler) {
        super(map, original);
        this.handler = handler;
    }

    /**
     * @return the attribute instance to which calls should be delegated
     */
    @Override
    protected EntityAttributeInstance getDelegateAttributeInstance() {
        if (handler.isPossessing()) {
            LivingEntity possessed = (LivingEntity) handler.getPossessedEntity();
            if (possessed != null) {
                EntityAttributeInstance ret = possessed.getAttributeInstance(this.getAttribute());
                // the attribute can be null if it is not registered in the possessed entity
                if (ret != null) {
                    return ret;
                }
            }
        }
        return super.getDelegateAttributeInstance();
    }
}
