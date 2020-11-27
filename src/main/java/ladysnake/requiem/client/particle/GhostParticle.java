package ladysnake.requiem.client.particle;

import ladysnake.requiem.client.render.RequiemRenderPhases;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class GhostParticle extends AbstractSlowingParticle {
    private static boolean renderedGhostParticle;
    private static final VertexConsumerProvider.Immediate ghostVertexConsumers = VertexConsumerProvider.immediate(
        new BufferBuilder(RequiemRenderPhases.GHOST_PARTICLE_LAYER.getExpectedBufferSize())
    );

    public static void draw(float tickDelta) {
        if (renderedGhostParticle) {
            RequiemRenderPhases.GHOST_PARTICLE_SHADER.render(tickDelta);
            RequiemRenderPhases.GHOST_PARTICLE_FRAMEBUFFER.clear();
            renderedGhostParticle = false;
        }
    }

    private final SpriteProvider spriteProvider;

    private GhostParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.scale(1.5F);
        // if the particle spawns within a block, ghost mode goes brr
        this.collidesWithWorld = !this.isColliding();
        this.setSpriteForAge(spriteProvider);
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        VertexConsumer actualConsumer = ghostVertexConsumers.getBuffer(RequiemRenderPhases.GHOST_PARTICLE_LAYER);
        super.buildGeometry(actualConsumer, camera, tickDelta);
        ghostVertexConsumers.draw();
        renderedGhostParticle = true;
    }

    public void tick() {
        super.tick();
        if (!this.dead) {
            this.setSpriteForAge(this.spriteProvider);
        }
    }

    public boolean isColliding() {
        return !this.world.isBlockSpaceEmpty(null, this.getBoundingBox(),
            (blockState, blockPosx) -> blockState.shouldSuffocate(this.world, blockPosx));
    }

    @Override
    public void setSpriteForAge(SpriteProvider spriteProvider) {
        if (this.collidesWithWorld || this.isColliding()) {
            this.setSprite(spriteProvider.getSprite(this.age / 2, this.maxAge));
        } else {
            this.setSprite(spriteProvider.getSprite(this.age / 2 + this.maxAge / 2, this.maxAge));
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType particleType, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            GhostParticle soulParticle = new GhostParticle(world, x, y, z, vx, vy, vz, this.spriteProvider);
            soulParticle.setColorAlpha(1.0F);
            return soulParticle;
        }
    }
}
