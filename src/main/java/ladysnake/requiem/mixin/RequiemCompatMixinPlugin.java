package ladysnake.requiem.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RequiemCompatMixinPlugin implements IMixinConfigPlugin {
    public static final boolean CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

    @Override
    public void onLoad(String mixinPackage) {
        // NO-OP
    }

    @Nullable
    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // NO-OP
    }

    @Override
    public List<String> getMixins() {
        return (new CompatBuilder())
            .addClient("healthoverlay", "HealthRendererMixin")
            .mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // NO-OP
    }

    private static final class CompatBuilder {
        private final List<String> mixins = new ArrayList<>();
        private final FabricLoader loader = FabricLoader.getInstance();

        CompatBuilder add(String modId, String path) {
            if (loader.isModLoaded(modId)) {
                this.mixins.add(modId + "." + path);
            }
            return this;
        }

        CompatBuilder addClient(String modId, String path) {
            if (CLIENT) {
                this.add(modId, path);
            }
            return this;
        }
    }
}
