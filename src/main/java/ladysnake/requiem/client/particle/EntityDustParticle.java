package ladysnake.requiem.client.particle;

import ladysnake.requiem.common.particle.RequiemEntityParticleEffect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class EntityDustParticle extends SpriteBillboardParticle {
    private final Entity target;
    private final float sampleU;
    private final float sampleV;

    public EntityDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, Entity target) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.target = target;
        this.setSprite(MinecraftClient.getInstance().getBlockRenderManager().getModels().getSprite(state));
        this.gravityStrength = 1.0F;
        this.colorRed = 0.6F;
        this.colorGreen = 0.6F;
        this.colorBlue = 0.6F;
        if (!state.isOf(Blocks.GRASS_BLOCK)) {
            int i = MinecraftClient.getInstance().getBlockColors().getColor(state, world, new BlockPos(x, y, z), 0);
            this.colorRed *= (float) (i >> 16 & 255) / 255.0F;
            this.colorGreen *= (float) (i >> 8 & 255) / 255.0F;
            this.colorBlue *= (float) (i & 255) / 255.0F;
        }

        this.scale /= 2.0F;
        this.sampleU = this.random.nextFloat() * 3.0F;
        this.sampleV = this.random.nextFloat() * 3.0F;
    }

    @Override
    public void tick() {
        Vec3d dir = this.target.getEyePos().subtract(this.x, this.y + 0.2, this.z).normalize();
        this.setVelocity(dir.x*0.8, dir.y*0.8, dir.z*0.8);
        super.tick();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.TERRAIN_SHEET;
    }

    @Override
    protected float getMinU() {
        return this.sprite.getFrameU((this.sampleU + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxU() {
        return this.sprite.getFrameU(this.sampleU / 4.0F * 16.0F);
    }

    @Override
    protected float getMinV() {
        return this.sprite.getFrameV(this.sampleV / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxV() {
        return this.sprite.getFrameV((this.sampleV + 1.0F) / 4.0F * 16.0F);
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<RequiemEntityParticleEffect> {
        @Override
        public Particle createParticle(RequiemEntityParticleEffect fx, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            BlockState blockState = fx.getBlockState();
            int entityId = fx.getTargetEntityId();
            Entity entity = world.getEntityById(entityId);
            return !blockState.isAir() && !blockState.isOf(Blocks.MOVING_PISTON) ? new EntityDustParticle(world, x, y, z, vx, vy, vz, blockState, entity) : null;
        }
    }
}
