package ladysnake.dissolution.client.renders.blocks;

import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.TartarosConfig;
import ladysnake.dissolution.common.blocks.BlockSoulAnchor;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.tileentities.TileEntitySoulAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RenderSoulAnchor extends TileEntitySpecialRenderer<TileEntitySoulAnchor> {
	
	private static final ResourceLocation TEXTURE;
	private static final String text = "ancre";
	private static int height = 2, width = 2;
	private static final ResourceLocation SOUL_PIPE_TEXTURE = new ResourceLocation(Reference.MOD_ID + ":textures/blocks/soul_anchor/soul_pipe.png");
	private static final ResourceLocation END_SKY_TEXTURE;
    private static final ResourceLocation END_PORTAL_TEXTURE;
    private static final Random RANDOM = new Random(31100L);
    private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    private FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
    
    static {
    	TEXTURE = new ResourceLocation(Reference.MOD_ID + ":textures/blocks/soul_anchor_special_render.png");
    	END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    	END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    }
	
	@Override
	public void renderTileEntityAt(TileEntitySoulAnchor te, double x, double y, double z, float partialTicks, int destroyStage) {
		
		//renderPortalAt(te, x, y, z, partialTicks);
		
		if(!TartarosConfig.anchorsXRay) return;
		
		//System.out.println(text);
		Minecraft mc = Minecraft.getMinecraft();
		
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(mc.player);
    	if(!playerCorp.isIncorporeal()) return;
    	
		renderSoulPipe(te, x, y, z);
		
		if(mc.player.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()), false, true, false) == null)
			return;
		
		GlStateManager.pushMatrix();
		
		Minecraft.getMinecraft().entityRenderer.disableLightmap();
		
		GlStateManager.translate(x, y, z);
		GlStateManager.enableRescaleNormal();
	    GlStateManager.disableDepth();
	    GlStateManager.disableLighting();
	    
	    GlStateManager.disableTexture2D();

	    this.bindTexture(END_SKY_TEXTURE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
	    GlStateManager.color(0.8f, 0.8f, 1f, 1.0f);
	    Tessellator tessellator = Tessellator.getInstance();
	    VertexBuffer tes = tessellator.getBuffer();
	    
	    tes.setTranslation(0.0, -Minecraft.getMinecraft().player.eyeHeight, 0.0);
	    
	    //First face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x, y + height, z).endVertex();
	    tes.pos(x + width, y + height, z).endVertex();
	    tes.pos(x + width, y, z).endVertex();
	    tes.pos(x, y, z).endVertex();
	    
	    tessellator.draw();
	    
	    //Second face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x, y, z + width).endVertex();
	    tes.pos(x, y + height, z + width).endVertex();
	    tes.pos(x, y + height, z).endVertex();
	    tes.pos(x, y, z).endVertex();
	    
	    tessellator.draw();
	    
	    //Third face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x + width, y + height, z + width).endVertex();
	    tes.pos(x, y + height, z + width).endVertex();
	    tes.pos(x, y, z + width).endVertex();
	    tes.pos(x + width, y, z + width).endVertex();
	    
	    tessellator.draw();
	    
	    //Fourth face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x + width, y, z).endVertex();
	    tes.pos(x + width, y + height, z).endVertex();
	    tes.pos(x + width, y + height, z + width).endVertex();
	    tes.pos(x + width, y, z + width).endVertex();
	    
	    tessellator.draw();
	    
	    //Bottom face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x, y, z).endVertex();
	    tes.pos(x + width, y, z).endVertex();
	    tes.pos(x + width, y, z + width).endVertex();
	    tes.pos(x, y, z + width).endVertex();
	    
	    tessellator.draw();
	    
	    //Top face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x, y + height, z).endVertex();
	    tes.pos(x, y + height, z + width).endVertex();
	    tes.pos(x + width, y + height, z + width).endVertex();
	    tes.pos(x + width, y + height, z).endVertex();
	    
	    tessellator.draw();

	    tes.setTranslation(0, 0, 0);
	    GlStateManager.enableTexture2D();

	    
	    /*GlStateManager.translate(x, y, z);
	    GlStateManager.rotate(180,1,0,0);
	    
	    FontRenderer fnt = mc.fontRendererObj;
	    float scale = 10 / fnt.FONT_HEIGHT;
	    GlStateManager.scale(scale, scale, scale);
	    GlStateManager.rotate(mc.getRenderManager().playerViewY + 180, 0.0F, 1.0F, 0.0F);
	    GlStateManager.rotate(-mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
	    
	    GlStateManager.translate(-fnt.getStringWidth(text) / 2, 0, 0);
	    fnt.drawString(text, 0, 0, 0x121212);
	    */
	    
	    GlStateManager.disableRescaleNormal();
	    Minecraft.getMinecraft().entityRenderer.enableLightmap();
	    
	    GlStateManager.popMatrix();
	}
	
	public void renderSoulPipe(TileEntitySoulAnchor te, double x, double y, double z) {
		GlStateManager.alphaFunc(516, 0.1F);
        this.bindTexture(SOUL_PIPE_TEXTURE);
        float f = 0;
        BlockPos targetPos = te.getExtremityPosition();
        double d0 = targetPos.getY() - y;
        f = MathHelper.sin(f * (float)Math.PI);
        int height = te.getPos().getY() - targetPos.getY();
        float[] afloat = EntitySheep.getDyeRgb(EnumDyeColor.WHITE);
        TileEntityBeaconRenderer.renderBeamSegment(x, y, z, 0, 1, 0, 1, -height, afloat, 0.30D, 0);
	}
	
	public void renderPortalAt(TileEntitySoulAnchor te, double x, double y, double z, float partialTicks)
    {
        GlStateManager.disableLighting();
        RANDOM.setSeed(31100L);
        GlStateManager.getFloat(2982, MODELVIEW);
        GlStateManager.getFloat(2983, PROJECTION);
        double d0 = x * x + y * y + z * z;
        int i = this.getPasses(d0);
        float f = this.getOffset();
        boolean flag = false;

        for (int j = 0; j < i; ++j)
        {
            GlStateManager.pushMatrix();
            float f1 = 2.0F / (float)(18 - j);
/*
            if (j == 0)
            {
                this.bindTexture(TEXTURE);
                f1 = 0.15F;
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }
*/
            if (j >= 1)
            {
                this.bindTexture(END_PORTAL_TEXTURE);
                flag = true;
                Minecraft.getMinecraft().entityRenderer.func_191514_d(true);
            }
/*
            if (j == 1)
            {
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }
*/
            GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 1.0F);
            float f2 = (float)(j + 1);
            GlStateManager.translate(17.0F / f2, (2.0F + f2 / 1.5F) * ((float)Minecraft.getSystemTime() % 800000.0F / 800000.0F), 0.0F);
            GlStateManager.rotate((f2 * f2 * 4321.0F + f2 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(4.5F - f2 / 4.0F, 4.5F - f2 / 4.0F, 1.0F);
            GlStateManager.multMatrix(PROJECTION);
            GlStateManager.multMatrix(MODELVIEW);
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            float f3 = (RANDOM.nextFloat() * 0.5F + 0.1F) * f1;
            float f4 = (RANDOM.nextFloat() * 0.5F + 0.4F) * f1;
            float f5 = (RANDOM.nextFloat() * 0.5F + 0.5F) * f1;

            if (te.shouldRenderFace(EnumFacing.SOUTH))
            {
                vertexbuffer.pos(x, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x + 1.0D, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.NORTH))
            {
                vertexbuffer.pos(x, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x + 1.0D, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x + 1.0D, y, z).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x, y, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.EAST))
            {
                vertexbuffer.pos(x + 1.0D, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x + 1.0D, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x + 1.0D, y, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.WEST))
            {
                vertexbuffer.pos(x, y, z).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.DOWN))
            {
                vertexbuffer.pos(x, y, z).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x + 1.0D, y, z).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x + 1.0D, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                vertexbuffer.pos(x, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.UP))
            {
            	if (te.getTargetDim() == 1 || true) {
	                vertexbuffer.pos(x, y + (double)f, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
	                vertexbuffer.pos(x + 1.0D, y + (double)f, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
	                vertexbuffer.pos(x + 1.0D, y + (double)f, z).color(f3, f4, f5, 1.0F).endVertex();
	                vertexbuffer.pos(x, y + (double)f, z).color(f3, f4, f5, 1.0F).endVertex();
            	}
            }

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            this.bindTexture(END_SKY_TEXTURE);
        }

        GlStateManager.disableBlend();
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
        GlStateManager.enableLighting();

        if (flag)
        {
            Minecraft.getMinecraft().entityRenderer.func_191514_d(false);
        }
    }
	
    protected int getPasses(double scaledPos_)
    {
        int i;

        if (scaledPos_ > 36864.0D)
        {
            i = 1;
        }
        else if (scaledPos_ > 25600.0D)
        {
            i = 3;
        }
        else if (scaledPos_ > 16384.0D)
        {
            i = 5;
        }
        else if (scaledPos_ > 9216.0D)
        {
            i = 7;
        }
        else if (scaledPos_ > 4096.0D)
        {
            i = 9;
        }
        else if (scaledPos_ > 1024.0D)
        {
            i = 11;
        }
        else if (scaledPos_ > 576.0D)
        {
            i = 13;
        }
        else if (scaledPos_ > 256.0D)
        {
            i = 14;
        }
        else
        {
            i = 15;
        }

        return i;
    }

    protected float getOffset()
    {
//        return 0.75F;
    	return (float) (Math.sin(Minecraft.getMinecraft().getSystemTime() / 1000f)) / 3f + 0.5f;
    }

    private FloatBuffer getBuffer(float p_147525_1_, float p_147525_2_, float p_147525_3_, float p_147525_4_)
    {
        this.buffer.clear();
        this.buffer.put(p_147525_1_).put(p_147525_2_).put(p_147525_3_).put(p_147525_4_);
        this.buffer.flip();
        return this.buffer;
    }
}
