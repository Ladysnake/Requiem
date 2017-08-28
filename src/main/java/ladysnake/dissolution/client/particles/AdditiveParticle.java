package ladysnake.dissolution.client.particles;

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
	
	private boolean additive;
	
	public static void spawnParticle(ResourceLocation texture, Entity spawner) {
		
	}
	
	public AdditiveParticle(World worldIn, double posXIn, double posYIn, double posZIn, double movX, double movY, double movZ, boolean additive) {
		super(worldIn, posXIn, posYIn, posZIn, movX, movY, movZ);
	    TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(STAR_PARTICLE_TEXTURE.toString());
	    this.setParticleTexture(sprite);
	}
	
	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public boolean isAdditive() {
		return true;
	}

}
