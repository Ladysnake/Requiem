package ladysnake.requiem.api.v1.remnant;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;

/**
 * @since 1.2.0
 */
public interface RemnantComponent extends AutoSyncedComponent, ServerTickingComponent {
    ComponentKey<RemnantComponent> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "remnant"), RemnantComponent.class);

    static boolean isIncorporeal(Entity entity) {
        RemnantComponent r = KEY.getNullable(entity);
        return r != null && r.isIncorporeal();
    }

    static boolean isSoul(Entity entity) {
        RemnantComponent r = KEY.getNullable(entity);
        return r != null && r.isSoul();
    }

    /**
     * Return a player's {@link RemnantState}. The remnant state is live, and
     * every modification made to it is reflected on the player.
     *
     * @return the player's remnant state
     * @since 1.2.0
     */
    @Contract(pure = true)
    static RemnantComponent get(PlayerEntity player) {
        return KEY.get(player);
    }

    /**
     * Make this player become the given {@link RemnantType type of remnant}.
     * <p>
     * If the given remnant type is the same as the current one, this method
     * does not have any visible effect. Otherwise, it will reset the current state,
     * replace it with a new one of the given type, and notify players of the change.
     * <p>
     * After this method has been called, the {@code RemnantType} returned by {@link #getRemnantType()}
     * will be {@code type}.
     *
     * @param type the remnant type to become
     * @see #getRemnantType()
     * @since 1.2.0
     */
    void become(RemnantType type);

    RemnantType getRemnantType();

    /**
     * Return whether this player is currently incorporeal.
     * A player is considered incorporeal if its current corporeality
     * is not tangible and they have no surrogate body.
     * @return true if the player is currently incorporeal, {@code false} otherwise
     */
    boolean isIncorporeal();

    boolean isSoul();

    void setSoul(boolean incorporeal);

    /**
     * Called when this remnant state's player is cloned
     *
     * @param original the player's clone
     * @param lossless false if the original player is dead, true otherwise
     */
    void copyFrom(ServerPlayerEntity original, boolean lossless);
}
