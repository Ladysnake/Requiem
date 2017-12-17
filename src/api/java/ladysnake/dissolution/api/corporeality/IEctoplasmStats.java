package ladysnake.dissolution.api.corporeality;

import java.util.List;

public interface IEctoplasmStats {
    List<SoulSpells> getActiveSpells();

    enum SoulSpells {
        FLIGHT
    }
}
