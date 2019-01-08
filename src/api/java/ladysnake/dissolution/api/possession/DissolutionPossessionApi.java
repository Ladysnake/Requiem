package ladysnake.dissolution.api.possession;

import ladysnake.dissolution.api.corporeality.IPossessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DissolutionPossessionApi {
    private static final Map<Class<? extends EntityLivingBase>, Class<? extends EntityLivingBase>> POSSESSABLES = new HashMap<>();

    /**
     * Excludes all the given entity classes from the possession mechanism. <br>
     * Players will be unable to possess any entity that has been excluded here,
     * unless a subsequent call to @{@link #registerPossessedVersion(Class, Class)} is made.
     * @param entityClasses entity classes that be excluded
     * @see #registerPossessedVersion(Class, Class)
     */
    @SafeVarargs
    public static void excludeEntitiesFromPossession(Class<? extends EntityLivingBase>... entityClasses) {
        for (Class<? extends EntityLivingBase> entityClass : entityClasses) {
            registerPossessedVersion(entityClass, null);
        }
    }

    /**
     * Use this method to register your own possessable version of an entity.
     * <p>
     *     When an entity is possessed by the player, the mod will try to replace it with an equivalent
     *     entity implementing {@link IPossessable}. If <tt>possessedEntityClass</tt> is <code>null</code>
     *     at the time of that check, no new entity will be instantiated and the player will be unable
     *     to possess it. <br>
     *     It is recommended that possessed versions inherit from their regular counterparts.
     * </p>
     * <p>
     *     Note that entities already implementing {@link IPossessable} will not be converted and will rather
     *     be possessed directly. Use {@link PossessionEvent} to handle these cases differently.
     * </p>
     * This method can be called at any time, entries will not be overridden when generating the new entity classes.<br>
     * Subsequent calls to this method will override the previous ones.
     * @param baseEntityClass the class of the regular entity
     * @param possessedEntityClass the class of an equivalent entity that can be possessed
     * @see #excludeEntitiesFromPossession(Class[])
     */
    public static <T extends EntityLivingBase, P extends EntityLivingBase & IPossessable> void registerPossessedVersion(Class<T> baseEntityClass, Class<P> possessedEntityClass) {
        POSSESSABLES.put(baseEntityClass, possessedEntityClass);
    }

    /**
     * Gets the possessable type associated with the given <code>base</code> or <code>null</code> if it was never
     * {@link #registerPossessedVersion(Class, Class) registered}.
     * @param base the base entity class
     * @param <T> the type of <code>base</code>
     * @param <P> the type of the associated class, implementing {@link IPossessable}
     * @return the generated possessable entity type
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends EntityLivingBase, P extends EntityLivingBase & IPossessable> Class<P> getPossessable(Class<T> base) {
        return (Class<P>) POSSESSABLES.get(base);
    }

    /**
     * Checks if a call to {@link #registerPossessedVersion(Class, Class)} has been made for the given
     * <code>entityClass</code>. <br>
     * The associated possessed entity class may still be <code>null</code>, the method just returns false
     * if there is no information regarding the given entity class.
     * @param entityClass a class of entity
     * @return true if the given class has been registered
     */
    public static boolean isEntityRegistered(Class<? extends Entity> entityClass) {
        return EntityLivingBase.class.isAssignableFrom(entityClass) && POSSESSABLES.containsKey(entityClass);
    }
}
