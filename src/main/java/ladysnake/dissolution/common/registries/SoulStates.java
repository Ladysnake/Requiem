package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.Ref;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = Ref.MOD_ID)
public class SoulStates {

    public static final ICorporealityStatus BODY;
    public static final ICorporealityStatus ECTOPLASM;
    public static final ICorporealityStatus SOUL;

    static {
        ModContainer old = Loader.instance().activeModContainer();
        Loader.instance().setActiveModContainer(Loader.instance().getIndexedModList().get(Ref.MOD_ID));
        SOUL = new SoulCorporealityStatus()
                .setRegistryName(Ref.MOD_ID, "soul");
        ECTOPLASM = new IncorporealStatus()
                .setRegistryName(Ref.MOD_ID, "ectoplasm");
        BODY = new CorporealityStatus(true, false)
                .setRegistryName(Ref.MOD_ID, "body");
        Loader.instance().setActiveModContainer(old);
    }

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
