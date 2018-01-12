package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class SoulStates {

    public static final ICorporealityStatus BODY = new CorporealityStatus(true, false)
            .setRegistryName(Reference.MOD_ID, "body");
    public static final ICorporealityStatus ECTOPLASM = new EctoplasmCorporealityStatus()
            .setRegistryName(Reference.MOD_ID, "ectoplasm");
    public static final ICorporealityStatus SOUL = new SoulCorporealityStatus()
            .setRegistryName(Reference.MOD_ID, "soul");

    public static IForgeRegistry<ICorporealityStatus> REGISTRY;

    @SubscribeEvent
    public static void onRegistryRegister(RegistryEvent.NewRegistry event) {
        REGISTRY = new RegistryBuilder<ICorporealityStatus>()
                .setName(new ResourceLocation("dissolution", "corporeality_statuses"))
                .setType(ICorporealityStatus.class).create();
    }

    @SubscribeEvent
    public static void onRegisterSetups(RegistryEvent.Register<ICorporealityStatus> event) {
        event.getRegistry().registerAll(BODY, ECTOPLASM, SOUL);
    }
}
