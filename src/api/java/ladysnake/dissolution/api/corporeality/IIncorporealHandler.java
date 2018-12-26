package ladysnake.dissolution.api.corporeality;

import ladysnake.dissolution.api.IDialogueStats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * The interface providing methods related to the Incorporeal capability
 * This is basically used as a replacement to extended properties
 *
 * @author Pyrofab
 */
public interface IIncorporealHandler {

    /**
     * If the player isn't <b>strong</b>, no mod mechanic should apply
     *
     * @return true if the player has taken the challenge of the passeresses
     */
    boolean isStrongSoul();

    void setStrongSoul(boolean strongSoul);

    /**
     * Sets the tangibility of the player specified, along with the corresponding attributes. <br/>
     * Also fires a {@link PlayerIncorporealEvent}
     * @param newStatus the new status to put the player in
     */
    void setCorporealityStatus(ICorporealityStatus newStatus);

    /**
     * @return The current status of this player
     */
    @Nonnull
    ICorporealityStatus getCorporealityStatus();

    default boolean isIncorporeal() {
        return getCorporealityStatus().isIncorporeal() && !isPossessionActive();
    }

    /**
     * @param possessable the entity to possess
     * @return false if the change could not occur
     * @throws IllegalArgumentException if the possessable argument is not an entity
     */
    default <T extends EntityLivingBase & IPossessable> boolean setPossessed(T possessable) {
        return setPossessed(possessable, false);
    }

    /**
     * @param possessable the entity to possess
     * @return false if the change could not occur
     * @throws IllegalArgumentException if the possessable argument is not an entity
     */
    <T extends EntityLivingBase & IPossessable> boolean setPossessed(T possessable, boolean force);

    <T extends EntityLivingBase & IPossessable> T getPossessed();

    UUID getPossessedUUID();

    default boolean isPossessionActive() {
        return getPossessedUUID() != null;
    }

    /**
     * Used for the dialogue when first entering a world
     */
    @Nonnull
    IDialogueStats getDialogueStats();

    IDeathStats getDeathStats();

    /**
     * Sets the synchronization status of this handler (between server and client)
     *
     * @param synced if true, this handler should be synchronized next tick
     */
    void setSynced(boolean synced);

    /**
     * Whether this handler needs updating from the server
     *
     * @return true if this handler has already been synchronized at least once
     */
    boolean isSynced();

    NBTTagCompound getSerializedPossessedEntity();

    void setSerializedPossessedEntity(NBTTagCompound serializedPossessedEntity);

    void tick();

}
