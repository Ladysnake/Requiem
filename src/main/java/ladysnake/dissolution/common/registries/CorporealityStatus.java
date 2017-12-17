package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class CorporealityStatus extends IForgeRegistryEntry.Impl<ICorporealityStatus> implements ICorporealityStatus {

    public static final ICorporealityStatus BODY = new CorporealityStatus(true, false)
            .setRegistryName(Reference.MOD_ID, "body");
    public static IForgeRegistry<ICorporealityStatus> REGISTRY;

    @SubscribeEvent
    public static void onRegistryRegister(RegistryEvent.NewRegistry event) {
        REGISTRY = new RegistryBuilder<ICorporealityStatus>()
                .setName(new ResourceLocation("dissolution", "corporeality_statuses"))
                .setType(ICorporealityStatus.class).create();
    }

    @SubscribeEvent
    public static void onRegisterSetups(RegistryEvent.Register<ICorporealityStatus> event) {
        event.getRegistry().registerAll(BODY, EctoplasmCorporealityStatus.ECTOPLASM, SoulCorporealityStatus.SOUL);
    }


    protected final Set<EntityPlayer> subscribedPlayers = new HashSet<>();
    private final boolean corporeal;
    private final boolean preventsEntityInteract;

    public CorporealityStatus(boolean corporeal, boolean preventsEntityInteract) {
        this.corporeal = corporeal;
        this.preventsEntityInteract = preventsEntityInteract;
    }

    public boolean isIncorporeal() {
        return !this.corporeal;
    }

    public String getUnlocalizedName() {
        return "dissolution.corporealitystatus." + this.toString();
    }

    @Override
    public boolean preventsInteraction(Entity entity) {
        return !preventsEntityInteract;
    }

    @Override
    public boolean allowsInvulnerability() {
        return false;
    }

    @Override
    public void initState(EntityPlayer player) {
        this.subscribedPlayers.add(player);
    }

    @Override
    public void resetState(EntityPlayer player) {
        this.subscribedPlayers.remove(player);
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }
}
