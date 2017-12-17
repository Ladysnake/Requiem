package ladysnake.dissolution.common.config;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.registries.CorporealityStatus;
import ladysnake.dissolution.common.registries.EctoplasmCorporealityStatus;
import ladysnake.dissolution.common.registries.SoulCorporealityStatus;

import java.util.Locale;

public enum EnumCorporealityStatus {
    BODY(CorporealityStatus.BODY),
    ECTOPLASM(EctoplasmCorporealityStatus.ECTOPLASM),
    SOUL(SoulCorporealityStatus.SOUL);

    public final ICorporealityStatus status;

    EnumCorporealityStatus(ICorporealityStatus corporealityStatus) {
        this.status = corporealityStatus;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }
}
