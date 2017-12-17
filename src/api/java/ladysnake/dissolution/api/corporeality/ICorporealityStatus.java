package ladysnake.dissolution.api.corporeality;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = "dissolution")
public interface ICorporealityStatus extends IForgeRegistryEntry<ICorporealityStatus> {


    boolean isIncorporeal();

    String getUnlocalizedName();

    boolean preventsInteraction(Entity entity);

    boolean allowsInvulnerability();

    void initState(EntityPlayer player);

    void resetState(EntityPlayer player);
}
