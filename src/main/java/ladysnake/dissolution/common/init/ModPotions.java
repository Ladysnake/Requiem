package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.potion.PotionPurification;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModPotions {

    public static final Potion PURIFICATION = new PotionPurification()
            .setPotionName("dissolution.potion.purification.name")
            .setRegistryName(Reference.MOD_ID, "purification");

    @SubscribeEvent
    public static void onRegistryRegister(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(PURIFICATION);
    }
}
