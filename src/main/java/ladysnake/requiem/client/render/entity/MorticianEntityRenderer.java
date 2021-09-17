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
package ladysnake.requiem.client.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.client.render.entity.model.MorticianEntityModel;
import ladysnake.requiem.common.entity.MorticianEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class MorticianEntityRenderer extends MobEntityRenderer<MorticianEntity, MorticianEntityModel<MorticianEntity>> {
    private static final Identifier TEXTURE = Requiem.id("textures/entity/mortician.png");

    public MorticianEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new MorticianEntityModel<>(ctx.getModelLoader().getModelPart(MorticianEntityModel.MODEL_LAYER)), 0.5F);
        this.addFeature(new HeadFeatureRenderer<>(this, ctx.getModelLoader()));
        this.addFeature(new VillagerHeldItemFeatureRenderer<>(this));
    }

    @Override
    public Identifier getTexture(MorticianEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MorticianEntity mortician, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (mortician.isObeliskProjection()) {
            float alpha = MathHelper.lerp(mortician.getFadingAmount(), 0.75f, 0f);
            RenderSystem.setShaderColor(1, 1, 1, alpha);
        }
        super.render(mortician, f, g, matrixStack, vertexConsumerProvider, i);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    protected void scale(MorticianEntity morticianEntity, MatrixStack matrixStack, float f) {
        matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
