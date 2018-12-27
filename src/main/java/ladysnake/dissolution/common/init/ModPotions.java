package ladysnake.dissolution.common.init;

import ladylib.registration.AutoRegister;
import ladysnake.dissolution.common.Ref;
import net.minecraft.potion.PotionType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
@AutoRegister(Ref.MOD_ID)
@GameRegistry.ObjectHolder(Ref.MOD_ID)
public class ModPotions {
    public static final PotionType OBNOXIOUS = new PotionType("dissolution.obnoxious");
}
