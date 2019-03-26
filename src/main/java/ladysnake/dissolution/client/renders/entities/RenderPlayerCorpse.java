package ladysnake.dissolution.client.renders.entities;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import ladylib.client.shader.ShaderRegistryEvent;
import ladysnake.dissolution.client.renders.ShaderHelper;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.entity.EntityPlayerShell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Ref.MOD_ID, value = Side.CLIENT)
public class RenderPlayerCorpse extends RenderBiped<EntityPlayerShell> {

    private boolean shouldRenderName = false;
    private static final ResourceLocation CORPSE_SHADER = new ResourceLocation(Ref.MOD_ID, "corpse");

    @SubscribeEvent
    public static void onShaderRegistry(ShaderRegistryEvent event) {
        event.registerShader(CORPSE_SHADER, new ResourceLocation(Ref.MOD_ID, "shaders/vertex_base.vsh"), new ResourceLocation(Ref.MOD_ID, "shaders/corpsedissolution.fsh"));
    }

    public RenderPlayerCorpse(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelPlayer(0.0F, true), 0.5F);
        this.addLayer(new LayerBipedArmor(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityPlayerShell entity) {
        ResourceLocation resourcelocation = DefaultPlayerSkin.getDefaultSkinLegacy();

        GameProfile profile = entity.getProfile();
        if (profile != null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(profile);

            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                resourcelocation = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            } else {
                UUID uuid = EntityPlayer.getUUID(profile);
                resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
            }
        }
        return resourcelocation;
    }

    @Override
    protected void preRenderCallback(EntityPlayerShell entitylivingbaseIn, float partialTickTime) {
        float f = 0.9375F;
        GlStateManager.scale(f, f, f);
    }

    @Override
    public void doRender(@Nonnull EntityPlayerShell entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        ShaderHelper.useShader(ShaderHelper.dissolution);
        ShaderHelper.setUniform("texture", 0);
        ShaderHelper.setUniform("lightmap", 1);
        int light = entity.getBrightnessForRender();
        int lightX = light % 65536;
        int lightY = light / 65536;
        ShaderHelper.setUniform("lightmapCoords", lightX, lightY);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        this.shouldRenderName = false;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        ShaderHelper.revert();
        this.shouldRenderName = true;
        this.renderName(entity, x, y, z);
        GlStateManager.popMatrix();
    }

    @Override
    protected float handleRotationFloat(EntityPlayerShell livingBase, float partialTicks) {
        return 20;
    }

    @Override
    protected boolean canRenderName(EntityPlayerShell entity) {
        return super.canRenderName(entity) && this.shouldRenderName;
    }

}
