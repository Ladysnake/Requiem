package ladysnake.dissolution.common.config;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.registries.CorporealityStatus;
import ladysnake.dissolution.common.registries.EctoplasmCorporealityStatus;
import ladysnake.dissolution.common.registries.SoulCorporealityStatus;

public enum EnumCorporealityStatus {
    BODY(CorporealityStatus.BODY),
    ECTOPLASM(EctoplasmCorporealityStatus.ECTOPLASM),
    SOUL(SoulCorporealityStatus.SOUL);

    public final ICorporealityStatus status;

    EnumCorporealityStatus(ICorporealityStatus corporealityStatus) {
        this.status = corporealityStatus;
    }
}
