package ladysnake.requiem.client;

import ladysnake.requiem.api.v1.remnant.FractureAnchor;
import ladysnake.requiem.common.impl.anchor.CommonAnchorManager;
import ladysnake.requiem.common.impl.anchor.InertFractureAnchor;
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
