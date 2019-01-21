package ladysnake.dissolution.common.impl.possession.asm;

import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.conversion.PossessableSubstitutionHandler;
import ladysnake.dissolution.api.v1.possession.conversion.PossessionRegistry;
import ladysnake.dissolution.common.entity.PossessableEntityImpl;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.minecraft.entity.EntityType;
import org.apiguardian.api.API;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * A factory generating classes from an implementation (think mixin) and a superclass
 * @param <T> the upper bound for accepted classes
 */
@API(status = INTERNAL)
public class MixedInSubclassFactory<T> {

    private final Class<? extends T> implementationClass;
    private final AsmClassLoader loader = new AsmClassLoader();
    // A cache containing every entity type created by this factory
    private final Map<Class<? extends T>, Class<? extends T>> generatedClasses = new HashMap<>();

    public MixedInSubclassFactory(Class<? extends T> implementationClass) {
        this.implementationClass = implementationClass;
    }

    /**
     * Creates a new class of {@link Possessable possessable} entity derived from the given base.
     * <p>
     * The new class will have the given base class as its superclass and implement {@link Possessable}, using
     * {@link PossessableEntityImpl} as a template.
     * @see PossessionRegistry#registerPossessedConverter(EntityType, PossessableSubstitutionHandler)
     */
    @SuppressWarnings("unchecked")
    public <C extends T> Class<? extends C> defineMixedInSubclass(Class<C> baseEntityClass) {
        return (Class<? extends C>) generatedClasses.computeIfAbsent(baseEntityClass, base -> {
            try {
                final byte[] possessableImplBytes = FabricLauncherBase.getLauncher().getClassByteArray(this.implementationClass.getName().replace('.', '/'));
                final ClassReader reader = new ClassReader(possessableImplBytes);
                final String name = getGeneratedName(base);
                ClassWriter writer = new ClassWriter(0);
                ClassVisitor adapter = new ChangeParentClassAdapter(Opcodes.ASM5, writer, name.replace('.', '/'), base.getName().replace('.', '/'), this.implementationClass);
                reader.accept(adapter, 0);

                return (Class) this.loader.define(name, writer.toByteArray());
            } catch (IOException e) {
                throw new UncheckedIOException("Could not obtain class bytes for " + this.implementationClass, e);
            }
        });
    }

    public Stream<Map.Entry<Class<? extends T>, Class<? extends T>>> getAllGeneratedClasses() {
        return generatedClasses.entrySet().stream();
    }

    private String getGeneratedName(Class baseClass) {
        return String.format("%s_%s", this.implementationClass.getName(), baseClass.getSimpleName());
    }

}
