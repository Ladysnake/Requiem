package ladysnake.dissolution.api.possession;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * PossessionEvent is fired when the possession state of a player changes.<br>
 * If a method utilizes this {@link Event} as its parameter, the method will
 * receive every child event of this class.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class PossessionEvent extends PlayerEvent {
    protected EntityLivingBase possessed;

    public <T extends EntityLivingBase & IPossessable> PossessionEvent(EntityPlayer player, T possessed) {
        super(player);
        this.possessed = possessed;
    }

    /**
     *
     * @param <T> intersection type of {@link EntityLivingBase} and {@link IPossessable} for convenience
     * @return the entity that is possessed by the player
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends EntityLivingBase & IPossessable> T getPossessed() {
        return (T) possessed;
    }

    /**
     * Setup is fired when a player attempts to possess an entity.<br>
     * This event is fired whenever a player attempts to possess an entity
     * by interacting with in while incorporeal.<br>
     * <p>
     * This event is fired during {@link EntityInteractSpecific}.<br>
     * This event is only fired server-side.
     * <br>
     * {@link #original} contains the {@link EntityLivingBase} that the player is attempting to possess. <br>
     * {@link #possessed} contains the {@link IPossessable} that will be actually possessed. <br>
     * <p>
     * The possessed entity may be the same as the original entity if the latter already implements {@link IPossessable}.<br>
     * A <code>null</code> possessed entity means that no acceptable substitute has been found.
     * If it stays (or becomes) null, the player will not possess the entity and the original will be left untouched.
     * <p>
     * This event is {@link Cancelable}.<br>
     * If it is canceled, the player will not possess the entity and the original will be left untouched.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     **/
    public static class Setup extends PossessionEvent {
        private final EntityLivingBase original;

        public <T extends EntityLivingBase & IPossessable> Setup(EntityPlayer player, @Nonnull EntityLivingBase original, @Nullable T possessed) {
            super(player, possessed);
            this.original = original;
        }

        /**
         * @return the original entity that the player tried to possess
         */
        public EntityLivingBase getOriginal() {
            return original;
        }

        /**
         * Sets the entity that will be possessed by the player
         * @param possessed an entity able to handle possession
         * @param <T> intersection type of {@link EntityLivingBase} and {@link IPossessable}
         */
        public <T extends EntityLivingBase & IPossessable> void setPossessed(T possessed) {
            this.possessed = possessed;
        }
    }

    /**
     * Start is fired when a player starts possessing an entity.<br>
     * This event is fired whenever a player starts possessing an entity in
     * {@link IIncorporealHandler#setPossessed(EntityLivingBase, boolean)}.<br>
     * <br>
     * {@link #possessed} contains the entity being possessed. <br>
     * <br>
     * This event is {@link Cancelable} if and only if the possession is not forced.<br>
     * If it is canceled, the player will not possess the entity.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Start extends PossessionEvent {
        private final boolean forced;

        public <T extends EntityLivingBase & IPossessable> Start(EntityPlayer player, @Nonnull T possessed, boolean forced) {
            super(player, possessed);
            this.forced = forced;
        }

        @Override
        public boolean isCancelable() {
            return !isForced();
        }

        public boolean isForced() {
            return forced;
        }

        /**
         *
         * @param <T> intersection type of {@link EntityLivingBase} and {@link IPossessable} for convenience
         * @return the entity that will be possessed by the player
         */
        @SuppressWarnings("unchecked")
        @Nonnull
        public <T extends EntityLivingBase & IPossessable> T getPossessed() {
            return (T) possessed;
        }
    }

    /**
     * Stop is fired when a player stops possessing an entity.<br>
     * <p>
     * This event is fired whenever a player's possessed entity is set to null in
     * {@link IIncorporealHandler#setPossessed(EntityLivingBase, boolean)}.<br>
     * <br>
     * {@link #possessed} contains the entity that was possessed. <br>
     * <br>
     * This event is {@link Cancelable} if and only if the possession is not forced.<br>
     * If it is canceled, the player will not stop possessing the entity.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Stop extends PossessionEvent {
        private final boolean forced;

        public <T extends EntityLivingBase & IPossessable> Stop(EntityPlayer player, T possessed, boolean forced) {
            super(player, possessed);
            this.forced = forced;
        }

        @Override
        public boolean isCancelable() {
            return !isForced();
        }

        public boolean isForced() {
            return forced;
        }


    }
}
