package ladysnake.dissolution.common.config;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.registries.SoulStates;

import java.util.Locale;

public enum EnumCorporealityStatus {
    BODY(SoulStates.BODY),
    ECTOPLASM(SoulStates.ECTOPLASM),
    SOUL(SoulStates.SOUL);

    public final ICorporealityStatus status;

    EnumCorporealityStatus(ICorporealityStatus corporealityStatus) {
        this.status = corporealityStatus;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }
}
