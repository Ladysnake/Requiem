package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.IEctoplasmStats;

import java.util.LinkedList;
import java.util.List;

public class EctoplasmStats implements IEctoplasmStats {

    private int health;
    private List<SoulSpells> unlockedSpells;
    private List<SoulSpells> activeSpells = new LinkedList<>();

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public List<SoulSpells> getActiveSpells() {
        return activeSpells;
    }

    public void setActiveSpells(List<SoulSpells> activeSpells) {
        this.activeSpells = activeSpells;
    }

}
