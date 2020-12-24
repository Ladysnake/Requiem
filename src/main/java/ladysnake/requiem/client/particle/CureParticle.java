package ladysnake.requiem.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;

public class CureParticle extends SpriteBillboardParticle {
    private boolean reachedGround;

    private CureParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.maxAge = (int)(20.0D / ((double)this.random.nextFloat() * 0.8D + 0.2D));
        this.reachedGround = false;
        this.collidesWithWorld = false;
    }

    // Taken from DragonBreathParticle
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            if (this.onGround) {
                this.velocityY = 0.0D;
                this.reachedGround = true;
            }

            if (this.reachedGround) {
                this.velocityY += 0.002;
            }

            this.move(this.velocityX, this.velocityY, this.velocityZ);

            if (this.y == this.prevPosY) {
                this.velocityX *= 1.1;
                this.velocityZ *= 1.1;
            }

            this.velocityX *= 0.95;
            this.velocityZ *= 0.95;

            if (this.reachedGround) {
                this.velocityY *= 0.95;
            }
        }
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    public float getSize(float tickDelta) {
        return this.scale * MathHelper.clamp((this.age + tickDelta) / this.maxAge * 32.0F, 0.0F, 1.0F);
    }

    // taken from FlameParticle
    public int getColorMultiplier(float tickDelta) {
        float progress = ((float)this.age + tickDelta) / (float)this.maxAge;
        progress = MathHelper.clamp(progress, 0.0F, 1.0F);
        int lightCoords = super.getColorMultiplier(tickDelta);
        int u = lightCoords & 255;
        int v = lightCoords >> 16 & 255;
        u += (int)(progress * 15.0F * 16.0F);
        if (u > 240) {
            u = 240;
        }

        return u | v << 16;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            CureParticle particle = new CureParticle(world, x, y, z, velocityX, velocityY, velocityZ);
            particle.setSpriteForAge(spriteProvider);
            return particle;
        }
    }
}
