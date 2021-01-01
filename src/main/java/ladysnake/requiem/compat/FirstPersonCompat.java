/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.compat;

import dev.tr7zw.firstperson.FirstPersonModelCore;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class FirstPersonCompat {
    private static final Field firstPersonModelCore$instance;
    private static final Method firstPersonModelCore$isFixActive;
    private static boolean fpmWorks = true;

    static {
        Field instance = null;
        Method isFixActive = null;
        try {
            instance = FirstPersonModelCore.class.getDeclaredField("instance");
            isFixActive = FirstPersonModelCore.class.getDeclaredMethod("isFixActive", Object.class, Object.class);
        } catch (NoClassDefFoundError | NoSuchFieldException | NoSuchMethodException e) {
            Requiem.LOGGER.error("[Requiem] First Person Model compatibility failed", e);
            fpmWorks = false;
        }
        firstPersonModelCore$instance = instance;
        firstPersonModelCore$isFixActive = isFixActive;
    }

    @CalledThroughReflection
    public static void init() {
        // static init
    }

    public static boolean setHeadVisibility(EntityModel<?> model, boolean visible) {
        if (model instanceof ModelWithHead) {
            ((ModelWithHead) model).getHead().visible = visible;
            return true;
        }
        return false;
    }

    public static boolean isFpmRenderingPlayer(LivingEntity livingEntity, MatrixStack matrixStack) {
        try {
            PlayerEntity possessor = ((Possessable) livingEntity).getPossessor();
            return possessor != null && fpmWorks && (Boolean) firstPersonModelCore$isFixActive.invoke(firstPersonModelCore$instance.get(null), possessor, matrixStack);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fpmWorks = false;
            Requiem.LOGGER.error("[Requiem] First Person Model compatibility failed", e);
            return false;
        }
    }
}
