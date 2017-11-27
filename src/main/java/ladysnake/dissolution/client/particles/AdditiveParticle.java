package ladysnake.dissolution.client.particles;

import ladysnake.dissolution.common.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.awt.*;

public class AdditiveParticle extends Particle implements IDissolutionParticle {

    public static final ResourceLocation STAR_PARTICLE_TEXTURE = new ResourceLocation(Reference.MOD_ID, "entity/aura");

    protected boolean additive;
    protected Color color = Color.WHITE;


    public AdditiveParticle(Entity spawner) {
        this(spawner, true);
    }

    public AdditiveParticle(Entity spawner, boolean additive) {
        this(spawner.world, spawner.posX, spawner.posY, spawner.posZ, additive);
    }

    public AdditiveParticle(World worldIn, double posX, double posY, double posZ, boolean additive) {
        super(worldIn, posX, posY, posZ);
        this.setTexture(null);
    }

    public AdditiveParticle(World world, float x, float y, float z, float scale, int lifetime, boolean additive) {
        this(world, x, y, z, true);
        this.particleScale = scale;
        this.particleMaxAge = lifetime;
    }

    public AdditiveParticle setMotion(float vx, float vy, float vz) {
        this.motionX = vx;
        this.motionY = vy;
        this.motionZ = vz;
        return this;
    }

    public AdditiveParticle giveRandomMotion(float xSpeedIn, float ySpeedIn, float zSpeedIn) {
        this.motionX = xSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        this.motionY = ySpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        this.motionZ = zSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        float f = (float) (Math.random() + Math.random() + 1.0D) * 0.15F;
        float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        this.motionX = this.motionX / (double) f1 * (double) f * 0.4000000059604645D;
        this.motionY = this.motionY / (double) f1 * (double) f * 0.4000000059604645D + 0.10000000149011612D;
        this.motionZ = this.motionZ / (double) f1 * (double) f * 0.4000000059604645D;
        return this;
    }

    public AdditiveParticle setTexture(ResourceLocation texture) {
        if (texture == null)
            texture = STAR_PARTICLE_TEXTURE;
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
        this.setParticleTexture(sprite);
        return this;
    }

    public AdditiveParticle setColor(Color color) {
        this.color = color;
        return this;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public boolean isAdditive() {
        return additive;
    }

}
