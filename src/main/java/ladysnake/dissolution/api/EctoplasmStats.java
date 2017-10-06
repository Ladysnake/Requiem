package ladysnake.dissolution.api;

import java.util.LinkedList;
import java.util.List;

public class EctoplasmStats {

    private int health;
    private List<SoulSpells> unlockedSpells;
    private List<SoulSpells> activeSpells = new LinkedList<>();

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public List<SoulSpells> getActiveSpells() {
        return activeSpells;
    }

    public void setActiveSpells(List<SoulSpells> activeSpells) {
        this.activeSpells = activeSpells;
    }

    public enum SoulSpells {
        FLIGHT
    }
}
