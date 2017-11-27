package ladysnake.dissolution.client.handlers;

import ladysnake.dissolution.common.Reference;
import net.minecraft.util.ResourceLocation;

public class AlbedoEventHandler {

    private static final ResourceLocation modularMachines = new ResourceLocation(Reference.MOD_ID, "modularmachinesetups");
    private static final ResourceLocation setupGreenhouse = new ResourceLocation(Reference.MOD_ID, "setup_greenhouse");

    // @SubscribeEvent
    // public static void onGatherLights(GatherLightsEvent event) {
    //     ((SetupGreenhouse) RegistryManager.ACTIVE.getRegistry(modularMachines)
    //             .getValue(setupGreenhouse))
    //             .allInstances.forEach(i -> event.getLightList().add(Light.builder()
    //                     .pos(i.getPosition().up())
    //                     .color(0.8f, 0.9f, 0.7f)
    //                     .radius(10).build()));
    // }
}
