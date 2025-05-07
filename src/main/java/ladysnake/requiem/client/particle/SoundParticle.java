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
package ladysnake.requiem.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.client.render.RequiemRenderPhases;
import ladysnake.requiem.common.particle.RequiemSoundParticleEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.awt.*;

public class SoundParticle extends SpriteBillboardParticle {
    public final SpriteProvider spriteProvider;
    private float red;
    private float green;
    private float blue;
    //private final ParticleTextureSheet sheet;
    //TODO: by:Redfan2: think about which VertexConsumer to use to not conflict with Darkness fog effect
    private static final VertexConsumerProvider.Immediate soundVertexConsumerProvider = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
    //Tessellator.getInstance().getBufferBuilder()
    //Doesnt render: Investigate: VertexConsumerProvider.immediate(new BufferBuilder(RequiemRenderPhases.GHOST_PARTICLE_LAYER.getExpectedBufferSize()));

    public SoundParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, int color) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setSpriteForAge(spriteProvider);
        //Testing
        Color tempColor = Color.BLUE;
        this.green = tempColor.getGreen();
        //this.sheet=new TextureSheet(spriteProvider);
        //Tessellator.getInstance().getBufferBuilder().
        this.red = tempColor.getRed();
        this.blue = tempColor.getBlue();
        this.maxAge = 10;
        this.collidesWithWorld = false;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        if (camera.getFocusedEntity() instanceof PlayerEntity player && player.getComponent(PossessionComponent.KEY).isPossessionOngoing()) {
            //TODO Disabled for testing if (player.getComponent(PossessionComponent.KEY).getHost() instanceof WardenEntity) {
            VertexConsumer actualConsumer = soundVertexConsumerProvider.getBuffer(RequiemRenderPhases.GHOST_PARTICLE_LAYER);

            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);


            Vec3d vec3d = camera.getPos();
            float f = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
            float g = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
            float h = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());
            Quaternionf quaternion2;
            if (this.angle == 0.0F) {
                quaternion2 = camera.getRotation();
            } else {
                quaternion2 = new Quaternionf(camera.getRotation());
                float i = MathHelper.lerp(tickDelta, this.prevAngle, this.angle);
                hamiltonProduct(quaternion2, Axis.Z_POSITIVE.rotation(i));
            }

            Vector3f Vec3f = new Vector3f(-1.0F, -1.0F, 0.0F);
            Vec3f.rotate(quaternion2);
            Vector3f[] Vec3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
            this.getSize(tickDelta);

            for (int k = 0; k < 4; ++k) {
                Vector3f Vec3f2 = Vec3fs[k];
                Vec3f2.rotate(quaternion2);
                Vec3f2.mul((float) new Vec3d(this.x, this.y, this.z).distanceTo(camera.getPos()) / 10f);
                Vec3f2.add(f, g, h);
            }

            float minU = this.getMinU();
            float maxU = this.getMaxU();
            float minV = this.getMinV();
            float maxV = this.getMaxV();
            int l = 15728880;
            float alpha = 1;

            actualConsumer.vertex(Vec3fs[0].x(), Vec3fs[0].y(), Vec3fs[0].z()).uv(maxU, maxV).color(red, green, blue, alpha).light(l).next();
            actualConsumer.vertex(Vec3fs[1].x(), Vec3fs[1].y(), Vec3fs[1].z()).uv(maxU, minV).color(red, green, blue, alpha).light(l).next();
            actualConsumer.vertex(Vec3fs[2].x(), Vec3fs[2].y(), Vec3fs[2].z()).uv(minU, minV).color(red, green, blue, alpha).light(l).next();
            actualConsumer.vertex(Vec3fs[3].x(), Vec3fs[3].y(), Vec3fs[3].z()).uv(minU, maxV).color(red, green, blue, alpha).light(l).next();


            soundVertexConsumerProvider.draw();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        //}
        } else {
            this.markDead();
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    public void tick() {
        this.setSpriteForAge(spriteProvider);

        if (this.age++ > this.maxAge) {
            this.markDead();
        }

        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
    }

    @ClientOnly
    public static class Factory implements ParticleFactory<RequiemSoundParticleEffect> {
        private final SpriteProvider factorySpriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.factorySpriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(RequiemSoundParticleEffect particleEffect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new SoundParticle(clientWorld, d, e, f, g, h, i, this.factorySpriteProvider, particleEffect.getColor());
        }
    }

    //Stolen using Linkie from Minecraft itself as it doesn't exist on JOML Quaternions
    public void hamiltonProduct(Quaternionf first, Quaternionf second) {
        float var2 = first.x();
        float var3 = first.y();
        float var4 = first.z();
        float var5 = first.w();
        float var6 = second.x();
        float var7 = second.y();
        float var8 = second.z();
        float var9 = second.w();
        first.x = var5 * var6 + var2 * var9 + var3 * var8 - var4 * var7;
        first.y = var5 * var7 - var2 * var8 + var3 * var9 + var4 * var6;
        first.z = var5 * var8 + var2 * var7 - var3 * var6 + var4 * var9;
        first.w = var5 * var9 - var2 * var6 - var3 * var7 - var4 * var8;
    }
}
