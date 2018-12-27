package ladysnake.dissolution.common.compat;

import com.legacy.aether.Aether;
import com.legacy.aether.AetherConfig;
import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public enum AetherLegacyCompat {
    @EnhancedBusSubscriber(value = Ref.MOD_ID, dependencies = Aether.modid) INSTANCE;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity traveller = event.getEntity();
        if (traveller.dimension == AetherConfig.getAetherDimensionID() && traveller.posY < -2) {
            // Too lazy to make this work properly, just remove the body if the player falls off the sky
            // (Issue: with default handling, the body is respawned at negative Y coordinates)
            // PRs welcome ofc
            CapabilityIncorporealHandler.getHandler(traveller).ifPresent(handler -> handler.setPossessed(null, true));
        }
    }
}
