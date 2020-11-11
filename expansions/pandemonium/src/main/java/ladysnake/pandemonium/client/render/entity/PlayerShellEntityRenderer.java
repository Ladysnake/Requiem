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

import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class PlayerShellEntityRenderer extends EntityRenderer<PlayerShellEntity> {
    public PlayerShellEntityRenderer(EntityRenderDispatcher renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public boolean shouldRender(PlayerShellEntity entity, Frustum frustum, double x, double y, double z) {
        ShellClientPlayerEntity renderedPlayer = entity.getRenderedPlayer();
        return this.dispatcher.getRenderer(renderedPlayer).shouldRender(renderedPlayer, frustum, x, y, z);
    }

    @Override
    public void render(PlayerShellEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        ShellClientPlayerEntity renderedPlayer = entity.getRenderedPlayer();
        this.dispatcher.getRenderer(renderedPlayer).render(renderedPlayer, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Vec3d getPositionOffset(PlayerShellEntity entity, float tickDelta) {
        ShellClientPlayerEntity renderedPlayer = entity.getRenderedPlayer();
        return this.dispatcher.getRenderer(renderedPlayer).getPositionOffset(renderedPlayer, tickDelta);
    }

    @Override
    public Identifier getTexture(PlayerShellEntity entity) {
        ShellClientPlayerEntity renderedPlayer = entity.getRenderedPlayer();
        return this.dispatcher.getRenderer(renderedPlayer).getTexture(renderedPlayer);
    }
}
