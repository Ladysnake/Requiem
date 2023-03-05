/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.client.render.entity;

import com.google.common.collect.ImmutableMap;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.entity.RequiemEntities;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PiglinEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

import java.util.Map;

public class CuredPiglinEntityRenderer extends PiglinEntityRenderer {
    private static final Map<EntityType<?>, Identifier> piglinTextures = ImmutableMap.of(
        RequiemEntities.CURED_PIGLIN, Requiem.id("textures/entity/cured_piglin.png"),
        RequiemEntities.CURED_PIGLIN_BRUTE, Requiem.id("textures/entity/cured_piglin_brute.png")
    );

    public CuredPiglinEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer mainLayer, EntityModelLayer innerArmorLayer, EntityModelLayer outerArmorLayer, boolean zombified) {
        super(ctx, mainLayer, innerArmorLayer, outerArmorLayer, zombified);
    }

    @Override
    public Identifier getTexture(MobEntity mobEntity) {
        Identifier identifier = piglinTextures.get(mobEntity.getType());
        if (identifier == null) {
            throw new IllegalArgumentException("I don't know what texture to use for " + mobEntity.getType());
        } else {
            return identifier;
        }
    }
}
