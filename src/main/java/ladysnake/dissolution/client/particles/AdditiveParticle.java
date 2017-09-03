package ladysnake.dissolution.client.particles;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.common.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class AdditiveParticle extends Particle implements IDissolutionParticle {
	
	public static final ResourceLocation STAR_PARTICLE_TEXTURE = new ResourceLocation(Reference.MOD_ID, "entity/particles/star");
	public static final ResourceLocation PINK_STAR_PARTICLE_TEXTURE = new ResourceLocation(Reference.MOD_ID, "entity/particles/star_purple");
	
	protected boolean additive;
	
	public AdditiveParticle(Entity spawner) {
		this(spawner, null, true);
	}
	
	public AdditiveParticle(Entity spawner, @Nullable ResourceLocation texture, boolean additive) {
		this(spawner.world, spawner.posX, spawner.posY, spawner.posZ, texture, additive);
	}
	
	public AdditiveParticle(World worldIn, double posX, double posY, double posZ, @Nullable ResourceLocation texture, boolean additive) {
		super(worldIn, posX, posY, posZ);
		applyProperties(texture, additive);
	}
	
	public AdditiveParticle(World worldIn, double posXIn, double posYIn, double posZIn, double movX, double movY, double movZ, @Nullable ResourceLocation texture, boolean additive) {
		super(worldIn, posXIn, posYIn, posZIn, movX, movY, movZ);
		applyProperties(texture, additive);
	}
	
	private void applyProperties(ResourceLocation texture, boolean additive) {
		if(texture == null)
			texture = STAR_PARTICLE_TEXTURE;
	    TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
	    this.setParticleTexture(sprite);
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
