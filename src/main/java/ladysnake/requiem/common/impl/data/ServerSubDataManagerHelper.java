package ladysnake.requiem.common.impl.data;

import ladysnake.requiem.api.v1.util.SubDataManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class ServerSubDataManagerHelper extends CommonSubDataManagerHelper {

    @Override
    public void registerSubDataManager(SubDataManager<?> serverManager) {
        super.registerSubDataManager(serverManager);
        ResourceManagerHelper.get(ResourceType.DATA).registerReloadListener(serverManager);
    }
}
