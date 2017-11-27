package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.boss.EntityBrimstoneFire;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RenderBrimstoneFire extends RenderEntity {

    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation(Reference.MOD_ID + ":textures/entity/projectiles/brimstone_beam.png");

    public RenderBrimstoneFire(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        if (!(entity instanceof EntityBrimstoneFire) || ((EntityBrimstoneFire) entity).getTarget() == BlockPos.ORIGIN)
            return;

        this.bindTexture(BEAM_TEXTURE);


        //TODO make that shit draw a beam

        BlockPos targetPos = ((EntityBrimstoneFire) entity).getTarget();
        float targX = targetPos.getX() + 0.5F;
        float targY = targetPos.getY() + 0.5F;
        float targZ = targetPos.getZ() + 0.5F;

        double posX = entity.posX;
        double posY = entity.posY;
        double posZ = entity.posZ;

        double deltaX = (double) targX - posX + x;
        double deltaY = (double) targY - posY + y;
        double deltaZ = (double) targZ - posZ + z;
        double deltaLength = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        float scale = 1;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GlStateManager.glTexParameteri(3553, 10242, 10497);
        GlStateManager.glTexParameteri(3553, 10243, 10497);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        float f1 = 240.0F;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        float f2 = (float) entity.world.getTotalWorldTime() + partialTicks;
        float f3 = f2 * 0.5F % 1.0F;
        float f4 = entity.getEyeHeight();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + f4, (float) z);
        Vec3d vec3d = new Vec3d(targetPos);
        Vec3d vec3d1 = this.getPosition((EntityBrimstoneFire) entity, (double) f4, partialTicks);
        Vec3d vec3d2 = vec3d.subtract(vec3d1);
        double d0 = vec3d2.lengthVector() + 1.0D;
        vec3d2 = vec3d2.normalize();
        float f5 = (float) Math.acos(vec3d2.y);
        float f6 = (float) Math.atan2(vec3d2.z, vec3d2.x);
        GlStateManager.rotate((((float) Math.PI / 2F) + -f6) * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f5 * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        int i = 1;
        double d1 = (double) f2 * 0.05D * -1.5D;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        float f7 = scale * scale;
        int j = 64 + (int) (f7 * 191.0F);
        int k = 32 + (int) (f7 * 191.0F);
        int l = 128 - (int) (f7 * 64.0F);
        double d2 = 0.2D;
        double d3 = 0.282D;
        double d4 = 0.0D + Math.cos(d1 + 2.356194490192345D) * 0.282D;
        double d5 = 0.0D + Math.sin(d1 + 2.356194490192345D) * 0.282D;
        double d6 = 0.0D + Math.cos(d1 + (Math.PI / 4D)) * 0.282D;
        double d7 = 0.0D + Math.sin(d1 + (Math.PI / 4D)) * 0.282D;
        double d8 = 0.0D + Math.cos(d1 + 3.9269908169872414D) * 0.282D;
        double d9 = 0.0D + Math.sin(d1 + 3.9269908169872414D) * 0.282D;
        double d10 = 0.0D + Math.cos(d1 + 5.497787143782138D) * 0.282D;
        double d11 = 0.0D + Math.sin(d1 + 5.497787143782138D) * 0.282D;
        double d12 = 0.0D + Math.cos(d1 + Math.PI) * 0.2D;
        double d13 = 0.0D + Math.sin(d1 + Math.PI) * 0.2D;
        double d14 = 0.0D + Math.cos(d1 + 0.0D) * 0.2D;
        double d15 = 0.0D + Math.sin(d1 + 0.0D) * 0.2D;
        double d16 = 0.0D + Math.cos(d1 + (Math.PI / 2D)) * 0.2D;
        double d17 = 0.0D + Math.sin(d1 + (Math.PI / 2D)) * 0.2D;
        double d18 = 0.0D + Math.cos(d1 + (Math.PI * 3D / 2D)) * 0.2D;
        double d19 = 0.0D + Math.sin(d1 + (Math.PI * 3D / 2D)) * 0.2D;
        double d20 = 0.0D;
        double d21 = 0.4999D;
        double d22 = (double) (-1.0F + f3);
        double d23 = d0 * 2.5D + d22;
        bufferbuilder.pos(d12, d0, d13).tex(0.4999D, d23).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d12, 0.0D, d13).tex(0.4999D, d22).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d14, 0.0D, d15).tex(0.0D, d22).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d14, d0, d15).tex(0.0D, d23).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d16, d0, d17).tex(0.4999D, d23).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d16, 0.0D, d17).tex(0.4999D, d22).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d18, 0.0D, d19).tex(0.0D, d22).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d18, d0, d19).tex(0.0D, d23).color(j, k, l, 255).endVertex();
        double d24 = 0.0D;

        if (entity.ticksExisted % 2 == 0) {
            d24 = 0.5D;
        }
        /*
        bufferbuilder.pos(d4, d0, d5).tex(0.5D, d24 + 0.5D).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d6, d0, d7).tex(1.0D, d24 + 0.5D).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d10, d0, d11).tex(1.0D, d24).color(j, k, l, 255).endVertex();
        bufferbuilder.pos(d8, d0, d9).tex(0.5D, d24).color(j, k, l, 255).endVertex();
        */
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    private Vec3d getPosition(EntityBrimstoneFire entityLivingBaseIn, double p_177110_2_, float p_177110_4_) {
        double d0 = entityLivingBaseIn.lastTickPosX + (entityLivingBaseIn.posX - entityLivingBaseIn.lastTickPosX) * (double) p_177110_4_;
        double d1 = p_177110_2_ + entityLivingBaseIn.lastTickPosY + (entityLivingBaseIn.posY - entityLivingBaseIn.lastTickPosY) * (double) p_177110_4_;
        double d2 = entityLivingBaseIn.lastTickPosZ + (entityLivingBaseIn.posZ - entityLivingBaseIn.lastTickPosZ) * (double) p_177110_4_;
        return new Vec3d(d0, d1, d2);
    }

}
