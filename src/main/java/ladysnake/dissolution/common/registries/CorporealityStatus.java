package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Objects;

public class CorporealityStatus extends IForgeRegistryEntry.Impl<ICorporealityStatus> implements ICorporealityStatus {

    private final boolean corporeal;
    protected final boolean preventsEntityInteract;

    public CorporealityStatus(boolean corporeal, boolean preventsEntityInteract) {
        this.corporeal = corporeal;
        this.preventsEntityInteract = preventsEntityInteract;
    }

    public boolean isIncorporeal() {
        return !this.corporeal;
    }

    public String getTranslationKey() {
        return "dissolution.corporealitystatus." + Objects.requireNonNull(this.getRegistryName()).getPath();
    }

    @Override
    public boolean allowsInvulnerability() {
        return false;
    }

    @Override
    public void initState(EntityPlayer player) {

    }

    @Override
    public void resetState(EntityPlayer player) {

    }

    @Override
    public String toString() {
        return this.getRegistryName() == null ? super.toString() : this.getRegistryName().toString();
    }
}
