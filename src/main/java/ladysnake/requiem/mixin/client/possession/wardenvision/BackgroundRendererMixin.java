/*
 * Requiem
 * Copyright (C) 2017-2024 Ladysnake
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
package ladysnake.requiem.mixin.client.possession.wardenvision;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.api.v1.possession.PossessedData;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.warden.WardenEntity;
import net.minecraft.util.math.MathHelper;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    /**The problem:
     * I think we should give the Warden the Darkness status effect when he is posessed by a Player. This would also imply us removing the effect after the posession has ended. Can we solve this simpler using a Mixin into the BackgroundRenderer?
     * If you know a good way to do this, let me know. I have had a look but couldn't come up with a solution without redirecting "Lnet/minecraft/client/render/BackgroundRenderer$FogEffect;fadeAsEffectWearsOff(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/effect/StatusEffectInstance;FF)F" on line 153 inside render(),
     * I am also unsure if it is a good idea to try to emulate the actions that are done on the FogEffect retrieved from the entity, meaning I am unsure if we should set the Fog Distances ourselves
     * @see net.minecraft.client.render.BackgroundRenderer#render(Camera, float, ClientWorld, int, float)
     * */
    @Unique
    private static final StatusEffectInstance EFFECT = new StatusEffectInstance(StatusEffects.DARKNESS,1,1);

    //Some stuff I thought about for potentially solving this
    @Redirect(
        at=@At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/BackgroundRenderer$FogEffect;fadeAsEffectWearsOff(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/effect/StatusEffectInstance;FF)F"
        ),
        method = "render"
    )
    private static float requiem$renderWardenDarkness(BackgroundRenderer.FogEffect fogEffect, LivingEntity entity, StatusEffectInstance effect, float horizonShading, float tickDelta) {
        if (entity instanceof WardenEntity) {
            return fogEffect.fadeAsEffectWearsOff(entity, EFFECT, horizonShading, 0.5f);
        }

        return fogEffect.fadeAsEffectWearsOff(entity, entity.getStatusEffect(StatusEffects.DARKNESS), horizonShading, tickDelta);
    }

    //This method by itself is useless as it is checked multiple other times whether the entity really has the needed StatusEffect
    @Inject(at=@At("HEAD"), method = "findFogEffect(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$FogEffect;", cancellable = true)
    private static void requiem$findDarknessFogEffect(Entity entity, float tickDelta, CallbackInfoReturnable<BackgroundRenderer.FogEffect> cir) {
        if (entity instanceof WardenEntity) {

            //TODO: by:Redfan2: We do have a AccessWidener for it, why doesn't it work?????
            //cir.setReturnValue(new BackgroundRenderer.DarknessFogEffect());

            //Temporary solution while testing
            cir.setReturnValue(BackgroundRenderer.FOG_EFFECTS.stream().filter(BackgroundRenderer.DarknessFogEffect.class::isInstance).toList().get(0));

        }
    }


}
