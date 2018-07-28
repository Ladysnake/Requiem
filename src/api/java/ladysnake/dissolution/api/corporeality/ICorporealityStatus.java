package ladysnake.dissolution.api.corporeality;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ICorporealityStatus extends IForgeRegistryEntry<ICorporealityStatus> {

    boolean isIncorporeal();

    String getTranslationKey();

    boolean allowsInvulnerability();

    void initState(EntityPlayer player);

    void resetState(EntityPlayer player);
}
