package ladysnake.dissolution.client;

import ladysnake.dissolution.api.v1.remnant.FractureAnchor;
import ladysnake.dissolution.common.impl.anchor.CommonAnchorManager;
import ladysnake.dissolution.common.impl.anchor.InertFractureAnchor;
import net.minecraft.world.World;

import java.util.UUID;

public class ClientAnchorManager extends CommonAnchorManager {
    public ClientAnchorManager(World world) {
        super(world);
    }

    public FractureAnchor getOrCreate(int id) {
        FractureAnchor ret = this.getAnchor(id);
        if (ret == null) {
            ret = addAnchor(InertFractureAnchor::new, UUID.randomUUID(), id);
        }
        return ret;
    }

}
