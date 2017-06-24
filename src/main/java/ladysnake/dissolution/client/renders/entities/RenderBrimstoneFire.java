package ladysnake.dissolution.client.renders.entities;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.EntityBrimstoneFire;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class RenderBrimstoneFire extends RenderEntity {
	
	private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation(Reference.MOD_ID + ":textures/entity/projectiles/brimstone_beam.png");

	public RenderBrimstoneFire(RenderManager renderManagerIn) {
		super(renderManagerIn);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		if(!(entity instanceof EntityBrimstoneFire) || ((EntityBrimstoneFire)entity).getTarget() == BlockPos.ORIGIN) 
			return;
		
		this.bindTexture(BEAM_TEXTURE);
		
		
		//TODO make that shit draw a beam
		
		BlockPos targetPos = ((EntityBrimstoneFire)entity).getTarget();
        float targX = targetPos.getX() + 0.5F;
        float targY = targetPos.getY() + 0.5F;
        float targZ = targetPos.getZ() + 0.5F;

        double posX = entity.posX;
        double posY = entity.posY;
        double posZ = entity.posZ;
        
        double deltaX = (double)targX - posX + x;
        double deltaY = (double)targY - posY + y;
        double deltaZ = (double)targZ - posZ + z;
        double deltaLength = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        
        final double angleZ = Math.PI - Math.atan2(deltaZ, deltaX);
        final double angleY;
        
        if (deltaX == 0 && deltaZ == 0) {
            final double angle = Math.PI / 2;
            if (deltaY < 0) {
                angleY = angle;
            } else {
                angleY = -angle;
            }
        } else {
            double deltaXY = Math.sqrt(deltaLength * deltaLength - deltaY * deltaY);
            angleY = -Math.atan2(deltaY, deltaXY);
        }

        final Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix4f holding = new Matrix4f();
        holding.setIdentity();
        
        // Translates the matrix forward by the start position
        Vector3f translation = new Vector3f();
        translation.x = (float) posX;
        translation.y = (float) posY;
        translation.z = (float) posZ;
        holding.setTranslation(translation);
        matrix.mul(holding);
        holding.setIdentity();

        // Scales the matrix
        holding.m00 = (float) 1;
        holding.m11 = (float) 1;
        holding.m22 = (float) 1;
        matrix.mul(holding);
        holding.setIdentity();

        // Rotates angles on the Z axis
        holding.rotY((float) angleZ);
        matrix.mul(holding);
        holding.setIdentity();

        // Rotates angles on the Y axis
        holding.rotZ((float) angleY);
        matrix.mul(holding);
        holding.setIdentity();
        
        //RenderDragon.renderCrystalBeams(d0, d1, d2, partialTicks, targX, targY, targZ, 0, posX, posY, posZ);
        
        double f0 = Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
        double f1 = Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ);

		GlStateManager.pushMatrix();
		//GlStateManager.rotate((float) ((-Math.atan2(d2, d0)) * (180.0 / Math.PI) - 90.0F), 0f, 1f, 0f);
		//GlStateManager.rotate((float) ((-Math.atan2(f0, d1)) * (180.0 / Math.PI) - 90.0F), 1f, 0f, 0f);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();
		
		buffer.setTranslation(x - posX, y - posY, z - posZ);
        
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		buffer.pos(0, 0, 0).color(0, 0, 0, 255).endVertex();
		buffer.pos(10, 0, 0).color(255, 255, 255, 255).endVertex();        
		buffer.pos(10, 10, 0).tex(1, 1).color(0, 1, 0, 1).endVertex();
		buffer.pos(0, 10, 0).tex(1, 1).color(0, 1, 0, 1).endVertex();
		
		tess.draw();
		
		buffer.setTranslation(0, 0, 0);
		GlStateManager.popMatrix();
	}

}
