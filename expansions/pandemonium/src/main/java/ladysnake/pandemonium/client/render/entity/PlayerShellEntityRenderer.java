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
package ladysnake.pandemonium.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.pandemonium.client.RequiemSkinManager;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;

public class PlayerShellEntityRenderer extends BipedEntityRenderer<PlayerShellEntity, PlayerEntityModel<PlayerShellEntity>> {

    public PlayerShellEntityRenderer(EntityRenderDispatcher renderManagerIn) {
        super(renderManagerIn, new PlayerEntityModel<>(0.0F, true), 0.5F);
        this.addFeature(new ArmorBipedFeatureRenderer<>(this, new BipedEntityModel<>(0.5F), new BipedEntityModel<>(1.0F)));
        this.addFeature(new HeldItemFeatureRenderer<>(this));
        this.addFeature(new StuckArrowsFeatureRenderer<>(this));
        this.addFeature(new HeadFeatureRenderer<>(this));
        this.addFeature(new ElytraFeatureRenderer<>(this));
    }

    @Override
    protected Identifier getTexture(PlayerShellEntity entity) {
        return RequiemSkinManager.get(entity.getProfile());
    }

    /**
     * Called before render
     */
    @Override
    protected void scale(PlayerShellEntity entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scalef(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public void render(@Nonnull PlayerShellEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.setProfile(GlStateManager.RenderMode.PLAYER_SKIN);
        super.render(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.unsetProfile(GlStateManager.RenderMode.PLAYER_SKIN);
    }

}
