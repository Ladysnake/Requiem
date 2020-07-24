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
package ladysnake.requiem.mixin.client.render.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.client.ShadowPlayerFx;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

// Note: this cannot use the right generics because of bridge methods
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererLayerMixin<T extends LivingEntity, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
    public PlayerRendererLayerMixin(EntityRenderDispatcher dispatcher, M model, float shadowRadius) {
        super(dispatcher, model, shadowRadius);
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Player rendering hijack part 2
     * We render incorporeal players on a different framebuffer for reuse.
     * The main framebuffer's depth has been previously copied to the alternate's,
     * so rendering should be visually equivalent.
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @Nullable
    @Intrinsic
    @Override
    protected RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean bl) {
        return super.getRenderLayer(entity, showBody, translucent, bl);
    }

    @SuppressWarnings("UnresolvedMixinReference")   // the method is injected through the intrinsic above
    @Inject(method = "getRenderLayer", at = @At("RETURN"), cancellable = true)
    private void replaceRenderLayer(T entity, boolean showBody, boolean translucent, boolean bl, CallbackInfoReturnable<RenderLayer> cir) {
        RequiemPlayer player = (RequiemPlayer) entity;
        if (player.asRemnant().isIncorporeal() || player.getDeathSuspender().isLifeTransient()) {
            cir.setReturnValue(ShadowPlayerFx.INSTANCE.getRenderLayer(cir.getReturnValue()));
        }
    }
}
