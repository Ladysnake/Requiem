package ladysnake.dissolution.common.compat;

import baubles.client.gui.GuiPlayerExpanded;
import baubles.common.Baubles;
import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

public enum BaublesCompat {
    @EnhancedBusSubscriber(value = Ref.MOD_ID, side = CLIENT, dependencies = Baubles.MODID) INSTANCE;

    @SubscribeEvent
    public void onInventoryRender(GuiScreenEvent.DrawScreenEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        GuiScreen gui = event.getGui();
        if (player != null && gui instanceof GuiPlayerExpanded) {
            GuiPlayerExpanded inv = (GuiPlayerExpanded) gui;
            int guiLeft = inv.getGuiLeft();
            int guiTop = inv.getGuiTop();
            EntityLivingBase possessed = CapabilityIncorporealHandler.getHandler(player).getPossessed();
            if (possessed != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GuiInventory.drawEntityOnScreen(guiLeft + 51, guiTop + 75, 30, guiLeft + 51 - event.getMouseX(), guiTop + 75 - 50 - event.getMouseY(), possessed);
            }
        }
    }
}
