package ladysnake.dissolution.common.impl.anchor;

import ladysnake.dissolution.api.v1.remnant.FractureAnchorManager;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;

public class TrackedFractureAnchor extends InertFractureAnchor {
    public TrackedFractureAnchor(FractureAnchorManager manager, UUID uuid, int id) {
        super(manager, uuid, id);
        if (!(manager.getWorld() instanceof ServerWorld)) {
            throw new IllegalArgumentException("EntityFractureAnchor is only supported on ServerWorld!");
        }
    }

    protected TrackedFractureAnchor(FractureAnchorManager manager, CompoundTag tag, int id) {
        super(manager, tag, id);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        syncWithWorld(createAnchorUpdateMessage(this));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        syncWithWorld(createAnchorDeleteMessage(this.getId()));
    }

    protected void syncWithWorld(CustomPayloadS2CPacket packet) {
        for (ServerPlayerEntity player : ((ServerWorld) this.manager.getWorld()).method_18456()) {
            sendTo(player, packet);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag anchorTag) {
        super.toTag(anchorTag);
        anchorTag.putString("AnchorType", "dissolution:tracked");
        return anchorTag;
    }
}
