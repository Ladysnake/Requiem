package ladysnake.dissolution.client;

import ladysnake.dissolution.api.v1.remnant.FractureAnchor;
import ladysnake.dissolution.api.v1.remnant.FractureAnchorFactory;
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
            ret = addAnchor(InertFractureAnchor::new, id);
        }
        return ret;
    }

    private FractureAnchor addAnchor(FractureAnchorFactory factory, int id) {
        return super.addAnchor(factory, UUID.randomUUID(), id);
    }

    @Override
    public void updateAnchors(long time) {
        // NO-OP
    }
}
