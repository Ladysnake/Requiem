package ladysnake.dissolution.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(1001)   // run after forge's runtime deobfuscation
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("ladysnake.dissolution.core.")
public class DissolutionLoadingPlugin implements IFMLLoadingPlugin {
    public static final Logger LOGGER = LogManager.getLogger("DissolutionCore");

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{DissolutionClassTransformer.class.getCanonicalName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
