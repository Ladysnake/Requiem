package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CorporealityStatus extends IForgeRegistryEntry.Impl<ICorporealityStatus> implements ICorporealityStatus {


    protected final Set<EntityPlayer> subscribedPlayers = new HashSet<>();
    private final boolean corporeal;
    protected final boolean preventsEntityInteract;

    public CorporealityStatus(boolean corporeal, boolean preventsEntityInteract) {
        this.corporeal = corporeal;
        this.preventsEntityInteract = preventsEntityInteract;
    }

    public boolean isIncorporeal() {
        return !this.corporeal;
    }

    public String getUnlocalizedName() {
        return "dissolution.corporealitystatus." + Objects.requireNonNull(this.getRegistryName()).getResourcePath();
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
        return this.getRegistryName() == null ? super.toString() : this.getRegistryName().toString();
    }
}
