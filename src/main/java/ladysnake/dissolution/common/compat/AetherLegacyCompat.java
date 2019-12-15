package ladysnake.dissolution.common.compat;

import com.legacy.aether.Aether;
import com.legacy.aether.AetherConfig;
import com.legacy.aether.client.gui.inventory.GuiAccessories;
import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

public enum AetherLegacyCompat {
    @EnhancedBusSubscriber(value = Ref.MOD_ID, dependencies = Aether.modid) INSTANCE;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity traveller = event.getEntity();
        if (traveller.dimension == AetherConfig.dimension.aether_dimension_id && traveller.posY < -2) {
            // Too lazy to make this work properly, just remove the body if the player falls off the sky
            // (Issue: with default handling, the body is respawned at negative Y coordinates)
            // PRs welcome ofc
            CapabilityIncorporealHandler.getHandler(traveller).ifPresent(handler -> handler.setPossessed(null, true));
        }
    }

    public enum Client {
        @EnhancedBusSubscriber(value = Ref.MOD_ID, side = CLIENT, dependencies = Aether.modid) INSTANCE;

        @SubscribeEvent
        public void onInventoryRender(GuiScreenEvent.DrawScreenEvent.Pre event) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            GuiScreen gui = event.getGui();
            if (player != null && gui instanceof GuiAccessories) {
                GuiAccessories inv = (GuiAccessories) gui;
                int guiLeft = inv.getGuiLeft();
                int guiTop = inv.getGuiTop();
                EntityLivingBase possessed = CapabilityIncorporealHandler.getHandler(player).getPossessed();
                if (possessed != null) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GuiInventory.drawEntityOnScreen(guiLeft + 35, guiTop + 75, 30, (float)(guiLeft + 51) - (float)event.getMouseX(), (float)(guiTop + 75 - 50) - (float)event.getMouseY(), possessed);
                }
            }
        }

    }
}
