package ladysnake.dissolution.common.impl.possession.asm;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.conversion.CopyStrategies;
import ladysnake.dissolution.api.v1.possession.conversion.PossessableConverterProvider;
import ladysnake.dissolution.api.v1.possession.conversion.PossessableSubstitutionHandler;
import ladysnake.dissolution.common.impl.possession.entity.PossessableEntityImpl;
import ladysnake.reflectivefabric.reflection.UncheckedReflectionException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.world.World;
import org.apiguardian.api.API;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Function;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public class AsmConverterProvider implements PossessableConverterProvider {
    private static final Class<?>[] WORLD_CONSTRUCTOR = {World.class};
    private static final Class<?>[] TYPE_WORLD_CONSTRUCTOR = {EntityType.class, World.class};

    private final MixedInSubclassFactory<MobEntity> factory = new MixedInSubclassFactory<>(PossessableEntityImpl.class);

    @Nullable
    @Override
    public <T extends MobEntity> PossessableSubstitutionHandler<T> get(EntityType<T> type, Class<T> baseClass) {
        Class<? extends T> generatedClass = factory.defineMixedInSubclass(baseClass);
        boolean hasWorldCnt = false;
        boolean hasTypeWorldCnt = false;
        // Look for a constructor that we can handle
        for (Constructor<?> constructor : baseClass.getDeclaredConstructors()) {
            if (Arrays.equals(TYPE_WORLD_CONSTRUCTOR, constructor.getParameterTypes())) {
                hasTypeWorldCnt = true;
            } else if (Arrays.equals(WORLD_CONSTRUCTOR, constructor.getParameterTypes())) {
                hasWorldCnt = true;
            }
        }
        Class<?> cntParam;
        Function<T, ?> pre;
        if (hasTypeWorldCnt) {
            pre = Function.identity();
            // Use the clone constructor from PossessableEntityImpl
            cntParam = MobEntityWithAi.class;
        } else if (hasWorldCnt) {
            pre = Entity::getEntityWorld;
            cntParam = World.class;
        } else {
            Dissolution.LOGGER.warn("Could not find a standard constructor for {} ({})", EntityType.getId(type), baseClass);
            return null;
        }
        Constructor<? extends T> cnt;
        try {
            cnt = generatedClass.getConstructor(cntParam);
        } catch (NoSuchMethodException e) {
            throw new UncheckedReflectionException("Generated possessable implementation is missing a constructor", e);
        }
        return PossessableSubstitutionHandler.using(
                // Generate the factory for that constructor as a Function<MobEntity, T>
                (o -> {
                    try {
                        return (MobEntityWithAi & Possessable) cnt.newInstance(pre.apply(o));
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new UncheckedReflectionException(e);
                    }
                }),
                CopyStrategies.nbtCopy(),
                PossessableSubstitutionHandler.swapEntities()
        );
    }
}
