package ladysnake.requiem.mixin.registration;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(Registry.class)
public interface RegistryAccessor {
    @Invoker("create")
    static <T> DefaultedRegistry<T> create(RegistryKey<Registry<T>> registryKey, String defaultId, Lifecycle lifecycle, Supplier<T> defaultEntry) {
        throw new UnsupportedOperationException("mixin was not transformed");
    }
}
