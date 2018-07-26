package ladysnake.dissolution.common.init;

import ladylib.registration.AutoRegister;
import ladysnake.dissolution.common.Reference;
import net.minecraft.potion.PotionType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
@AutoRegister(Reference.MOD_ID)
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModPotions {

//    public static final Potion PURIFICATION = new PotionPurification()
//            .setPotionName("dissolution.potion.purification.name")
//            .setRegistryName(Reference.MOD_ID, "purification");

    public static final PotionType OBNOXIOUS = new PotionType("dissolution.obnoxious");
}
