package ladysnake.requiem.common.impl.anchor;

import ladysnake.requiem.api.v1.remnant.FractureAnchorManager;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

import static ladysnake.requiem.common.network.RequiemNetworking.*;

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
        for (ServerPlayerEntity player : ((ServerWorld) this.manager.getWorld()).getPlayers()) {
            sendTo(player, packet);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag anchorTag) {
        super.toTag(anchorTag);
        anchorTag.putString("AnchorType", "requiem:tracked");
        return anchorTag;
    }
}
