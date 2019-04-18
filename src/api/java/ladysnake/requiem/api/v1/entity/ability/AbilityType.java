package ladysnake.requiem.api.v1.entity.ability;

public enum AbilityType {
    /**
     * {@code ATTACK} abilities are triggered by players attempting to hit something.
     * They usually result in entity/terrain damage.
     */
    ATTACK,
    /**
     * {@code INTERACT} abilities are triggered by players attempting to interact with something.
     * Their effects are broader than {@link #ATTACK}, ranging from spellcasting to teleporting.
     */
    INTERACT
}
