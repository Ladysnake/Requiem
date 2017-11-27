package ladysnake.dissolution.client.renders.entities;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class LayerDisguise implements LayerRenderer<EntityPlayer> {

    private final RenderPlayer renderer;
    private final ModelPlayer layerModel;
    private Map<UUID, ResourceLocation> usurpedPlayerSkins;

    public LayerDisguise(RenderPlayer renderer, boolean smallArms) {
        super();
        this.renderer = renderer;
        this.layerModel = new ModelPlayer(0f, false);
        this.usurpedPlayerSkins = new HashMap<>();
    }

    @Override
    public void doRenderLayer(EntityPlayer playerIn, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        CapabilityIncorporealHandler.getHandler(playerIn).getDisguise().ifPresent(uuid -> {
            this.layerModel.setModelAttributes(this.renderer.getMainModel());
            this.layerModel.setLivingAnimations(playerIn, limbSwing, limbSwingAmount, partialTicks);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.renderer.bindTexture(getSkin(playerIn.world, uuid));
            this.layerModel.render(playerIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        });
    }

    private ResourceLocation getSkin(World worldIn, UUID usurpedPlayer) {
        return usurpedPlayerSkins.computeIfAbsent(usurpedPlayer,
                id -> new EntityOtherPlayerMP(worldIn, new GameProfile(id, "")).getLocationSkin());
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }

}
