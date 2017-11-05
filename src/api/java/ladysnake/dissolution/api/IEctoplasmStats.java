package ladysnake.dissolution.api;

import java.util.List;

public interface IEctoplasmStats {
    List<SoulSpells> getActiveSpells();

    enum SoulSpells {
        FLIGHT
    }
}
