package ladysnake.dissolution.core;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DependencyParser;

import java.util.List;
import java.util.Set;

public class DissolutionCore extends DummyModContainer {
    public static final String MOD_ID = "dissolutioncore";
    public static final String VERSION = "1.12.2-1.0.0.0";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.3.2665,)";

    public DissolutionCore() {
        super(new ModMetadata());
        ModMetadata metadata = getMetadata();
        metadata.modId = MOD_ID;
        metadata.name = "Dissolution Core Mod";
        metadata.version = VERSION;
        metadata.authorList.add("Pyrofab");
        metadata.description = "Allows Dissolution's possession system to work somewhat properly";
        metadata.screenshots = new String[0];

        DependencyParser dependencyParser = new DependencyParser(getModId(), FMLCommonHandler.instance().getSide());
        DependencyParser.DependencyInfo info = dependencyParser.parseDependencies(DEPENDENCIES);
        metadata.requiredMods = info.requirements;
        metadata.dependencies = info.dependencies;
        metadata.dependants = info.dependants;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Override
    public Set<ArtifactVersion> getRequirements() {
        return getMetadata().requiredMods;
    }

    @Override
    public List<ArtifactVersion> getDependencies() {
        return getMetadata().dependencies;
    }

    @Override
    public List<ArtifactVersion> getDependants() {
        return getMetadata().dependants;
    }
}
