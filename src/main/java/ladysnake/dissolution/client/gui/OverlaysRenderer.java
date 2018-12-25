package ladysnake.dissolution.client.gui;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OverlaysRenderer {

    public static final OverlaysRenderer INSTANCE = new OverlaysRenderer();

    void renderOverlays(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);
        EntityLivingBase possessed = playerCorp.getPossessed();
        if (possessed != null && mc.gameSettings.thirdPersonView == 0) {
            mc.getItemRenderer().renderOverlays(partialTicks);
            if (possessed.isBurning()) {
                if (!net.minecraftforge.event.ForgeEventFactory.renderFireOverlay(mc.player, partialTicks)) {
                    mc.getItemRenderer().renderFireInFirstPerson();
                }
            }
        }
    }
}
