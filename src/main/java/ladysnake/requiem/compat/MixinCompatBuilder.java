package ladysnake.requiem.compat;

import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.List;

public final class MixinCompatBuilder {
    private final List<String> mixins = new ArrayList<>();
    private final FabricLoader loader = FabricLoader.getInstance();

    public MixinCompatBuilder add(String modId, String path) {
        if (loader.isModLoaded(modId)) {
            this.mixins.add(modId + "." + path);
        }
        return this;
    }

    public MixinCompatBuilder addClient(String modId, String path) {
        if (RequiemCompatMixinPlugin.CLIENT) {
            this.add(modId, path);
        }
        return this;
    }

    public List<String> build() {
        return this.mixins;
    }
}
