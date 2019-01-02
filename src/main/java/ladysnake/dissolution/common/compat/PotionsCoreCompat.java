package ladysnake.dissolution.common.compat;

import com.jamieswhiteshirt.clothesline.hooks.api.GetMouseOverEvent;
import com.tmtravlr.potioncore.PotionCore;
import com.tmtravlr.potioncore.PotionCoreEntityRenderer;
import ladylib.compat.EnhancedBusSubscriber;
import ladylib.compat.StateEventReceiver;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

/**
 * Potions Core compatibility: Fix "Attempting to attack an invalid entity" when left-clicking during possession
 */
public enum  PotionsCoreCompat implements StateEventReceiver {
    @EnhancedBusSubscriber(value = Ref.MOD_ID, side = CLIENT, dependencies = PotionCore.MOD_ID) INSTANCE;

    private EntityRenderer potionCoreEntityRenderer;

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.entityRenderer instanceof PotionCoreEntityRenderer) {
            potionCoreEntityRenderer = mc.entityRenderer;
            mc.entityRenderer = new EntityRenderer(mc, mc.getResourceManager());    // get the entity renderer back in place
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onGetMouseOver(GetMouseOverEvent event) {
        if (potionCoreEntityRenderer != null) {
            Minecraft mc = Minecraft.getMinecraft();
            // Yup, EntityRenderer#getMouseOver -> PotionCoreEntityRenderer#getMouseOver -> PotionCoreEntityRenderer#getMouseOver
            // Honestly, if you don't want this, just disable fixRange in PotionCore.
            potionCoreEntityRenderer.getMouseOver(event.getPartialTicks());
            if (CapabilityIncorporealHandler.getHandler(mc.pointedEntity).filter(IIncorporealHandler::isIncorporeal).isPresent()) {
                NetworkPlayerInfo info = mc.getConnection().getPlayerInfo(((EntityPlayer)mc.pointedEntity).getGameProfile().getId());
                GameType currentGm = info.getGameType();
                // spectators cannot be targeted
                info.setGameType(GameType.SPECTATOR);
                potionCoreEntityRenderer.getMouseOver(event.getPartialTicks());
                info.setGameType(currentGm);
            }
        }
    }
}
