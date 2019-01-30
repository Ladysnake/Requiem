package ladysnake.dissolution.common.impl.possession.asm;

import ladysnake.dissolution.api.v1.possession.conversion.PossessableConverterProvider;
import ladysnake.dissolution.api.v1.possession.conversion.PossessableSubstitutionHandler;
import ladysnake.dissolution.common.entity.PossessableEntityImpl;
import ladysnake.dissolution.common.impl.possession.CopyStrategies;
import ladysnake.dissolution.common.impl.possession.Possession;
import ladysnake.reflectivefabric.reflection.ReflectionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.apiguardian.api.API;

import javax.annotation.Nullable;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.function.Function;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public class AsmConverterProvider implements PossessableConverterProvider {
    private static final Class<?>[] WORLD_CONSTRUCTOR = {World.class};

    private final MixedInSubclassFactory<MobEntity> factory = new MixedInSubclassFactory<>(PossessableEntityImpl.class);

    @Nullable
    @Override
    public <T extends MobEntity> PossessableSubstitutionHandler<T> get(EntityType<T> type) {
        Class<? extends T> baseClass = type.getEntityClass();
        Class<? extends T> generatedClass = factory.defineMixedInSubclass(baseClass);
        Function<T, World> worldGetter = Entity::getEntityWorld;
        // Look for a constructor that we can handle
        for (Constructor<?> constructor : baseClass.getDeclaredConstructors()) {
            if (Arrays.equals(WORLD_CONSTRUCTOR, constructor.getParameterTypes())) {
                return PossessableSubstitutionHandler.using(
                        // Generate the factory for that constructor as a Function<World, T>
                        // Use a trusted lookup in case of an inaccessible constructor
                        worldGetter.andThen(ReflectionHelper.createFactory(
                                generatedClass,
                                "apply",
                                Function.class,
                                ReflectionHelper.getTrustedLookup(generatedClass),
                                MethodType.methodType(Object.class, Object.class),
                                World.class
                        )),
                        CopyStrategies.nbtCopy(),
                        Possession.swapEntities()
                );
            }
        }

        return null;
    }
}
