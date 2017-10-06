package ladysnake.dissolution.client.handlers;

import elucent.albedo.event.GatherLightsEvent;
import elucent.albedo.lighting.Light;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.registries.modularsetups.SetupGreenhouse;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.RegistryManager;

public class AlbedoEventHandler {

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void onGatherLights(GatherLightsEvent event) {
        ((SetupGreenhouse) RegistryManager.ACTIVE.getRegistry(new ResourceLocation(Reference.MOD_ID, "modularmachinesetups"))
                .getValue(new ResourceLocation(Reference.MOD_ID, "setup_greenhouse")))
                .allInstances.forEach(i -> event.getLightList().add(Light.builder()
                        .pos(i.getPosition().up())
                        .color(0.8f, 0.9f, 0.7f)
                        .radius(10).build()));
    }
}
