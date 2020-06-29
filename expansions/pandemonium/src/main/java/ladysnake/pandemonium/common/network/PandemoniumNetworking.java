package ladysnake.pandemonium.common.network;

import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.requiem.Requiem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;

import static ladysnake.requiem.common.network.RequiemNetworking.createEmptyBuffer;

public class PandemoniumNetworking {
    public static final Identifier ETHEREAL_ANIMATION = Requiem.id("ethereal_animation");
    public static final Identifier ANCHOR_DAMAGE = Requiem.id("anchor_damage");
    public static final Identifier ANCHOR_SYNC = Requiem.id("anchor_sync_update");
    public static final Identifier ANCHOR_REMOVE = Requiem.id("anchor_sync_remove");

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createAnchorDamageMessage(boolean dead) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeBoolean(dead);
        return new CustomPayloadS2CPacket(ANCHOR_DAMAGE, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createAnchorDeleteMessage(int anchorId) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeInt(anchorId);
        return new CustomPayloadS2CPacket(ANCHOR_REMOVE, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createAnchorUpdateMessage(FractureAnchor anchor) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeInt(anchor.getId());
        buf.writeDouble(anchor.getX());
        buf.writeDouble(anchor.getY());
        buf.writeDouble(anchor.getZ());
        return new CustomPayloadS2CPacket(ANCHOR_SYNC, buf);
    }
}
