package ladysnake.dissolution.api.corporeality;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod.EventBusSubscriber(modid = "dissolution")
public interface ICorporealityStatus extends IForgeRegistryEntry<ICorporealityStatus> {

    boolean isIncorporeal();

    String getUnlocalizedName();

    boolean allowsInvulnerability();

    void initState(EntityPlayer player);

    void resetState(EntityPlayer player);
}
