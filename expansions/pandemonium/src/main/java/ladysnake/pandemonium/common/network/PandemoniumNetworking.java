package ladysnake.pandemonium.common.network;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static ladysnake.requiem.common.network.RequiemNetworking.createEmptyBuffer;
import static ladysnake.requiem.common.network.RequiemNetworking.createEmptyMessage;

public final class PandemoniumNetworking {
    public static final Identifier ETHEREAL_ANIMATION = Requiem.id("ethereal_animation");
    public static final Identifier ANCHOR_DAMAGE = Requiem.id("anchor_damage");

    public static void sendAnchorDamageMessage(ServerPlayerEntity player, boolean dead) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeBoolean(dead);
        RequiemNetworking.sendTo(player, new CustomPayloadS2CPacket(ANCHOR_DAMAGE, buf));
    }

    public static void sendEtherealAnimationMessage(ServerPlayerEntity player) {
        RequiemNetworking.sendTo(player, createEmptyMessage(PandemoniumNetworking.ETHEREAL_ANIMATION));
    }
}
