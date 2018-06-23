package ladysnake.dissolution.unused.client.renders.entities;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.client.renders.ShaderHelper;
import ladysnake.dissolution.common.entity.EntityPlayerShell;
import ladysnake.dissolution.unused.client.models.entities.ModelMinionZombie;
import ladysnake.dissolution.unused.client.models.entities.ModelPlayerCorpse;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class RenderPlayerCorpse extends RenderBiped<EntityPlayerShell> {

    private Map<String, ResourceLocation> texture = new HashMap<>();
    private boolean shouldRenderName = false;

    public RenderPlayerCorpse(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelPlayerCorpse(0.0F, true), 0.5F);
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
            protected void initArmor() {
                this.modelLeggings = new ModelMinionZombie(0.5F, true);
                this.modelArmor = new ModelMinionZombie(1.0F, true);
            }
        };
        this.addLayer(layerbipedarmor);
    }

    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityPlayerShell entity) {
        if (entity.hasCustomName()) {
            try {
                return texture.computeIfAbsent(entity.getCustomNameTag(),
                        name -> new EntityOtherPlayerMP(entity.world, new GameProfile(null, name)).getLocationSkin());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return DefaultPlayerSkin.getDefaultSkinLegacy();
    }

    @Override
    protected void preRenderCallback(EntityPlayerShell entitylivingbaseIn, float partialTickTime) {
    }

    @Override
    public void doRender(@Nonnull EntityPlayerShell entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        ShaderHelper.useShader(ShaderHelper.dissolution);
        ShaderHelper.setUniform("texture", 0);
        ShaderHelper.setUniform("lightmap", OpenGlHelper.lightmapTexUnit);
        //System.out.println(Math.abs(entity.getRemainingTicks() / (float) entity.getMaxTimeRemaining()));
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        float light = Math.max(entity.world.getLightFor(EnumSkyBlock.SKY, entity.getPosition()) * entity.world.getSunBrightnessFactor(1.0f),
                entity.world.getLightFor(EnumSkyBlock.BLOCK, entity.getPosition()));
        ShaderHelper.setUniform("lighting", light);
        this.shouldRenderName = false;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        ShaderHelper.revert();
        this.shouldRenderName = true;
        this.renderName(entity, x, y, z);
        GlStateManager.popMatrix();
    }

    @Override
    protected boolean canRenderName(EntityPlayerShell entity) {
        return super.canRenderName(entity) && this.shouldRenderName;
    }

}
