package ladysnake.dissolution.common.network;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.remnant.FractureAnchor;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.jetbrains.annotations.Contract;

import java.util.UUID;

import static io.netty.buffer.Unpooled.buffer;

public class DissolutionNetworking {
    // Server -> Client
    public static final Identifier ANCHOR_DAMAGE = Dissolution.id("anchor_damage");
    public static final Identifier ANCHOR_SYNC = Dissolution.id("anchor_sync_update");
    public static final Identifier ANCHOR_REMOVE = Dissolution.id("anchor_sync_remove");
    public static final Identifier ETHEREAL_ANIMATION = Dissolution.id("ethereal_animation");
    public static final Identifier POSSESSION_SYNC = Dissolution.id("possession_sync");
    public static final Identifier REMNANT_SYNC = Dissolution.id("remnant_sync");

    // Client -> Server
    public static final Identifier LEFT_CLICK_AIR = Dissolution.id("attack_air");
    public static final Identifier RIGHT_CLICK_AIR = Dissolution.id("interact_air");
    public static final Identifier POSSESSION_REQUEST = Dissolution.id("possession_request");
    public static final Identifier ETHEREAL_FRACTURE = Dissolution.id("ethereal_fracture");

    public static void sendToServer(Identifier identifier, PacketByteBuf data) {
        MinecraftClient.getInstance().player.networkHandler.sendPacket(new CustomPayloadC2SPacket(identifier, data));
        data.release();
    }

    public static void sendTo(ServerPlayerEntity player, CustomPayloadS2CPacket message) {
        sendToPlayer(player, message);
        message.getData().release();
    }

    public static void sendToAllTrackingIncluding(Entity tracked, CustomPayloadS2CPacket message) {
        if (tracked.world instanceof ServerWorld) {
            PlayerStream.watching(tracked).forEach(p -> sendToPlayer((ServerPlayerEntity) p, message));
            if (tracked instanceof ServerPlayerEntity) {
                sendToPlayer((ServerPlayerEntity) tracked, message);
            }
        }
        message.getData().release();
    }

    private static void sendToPlayer(ServerPlayerEntity player, CustomPayloadS2CPacket message) {
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(message);
        }
    }

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createCorporealityMessage(PlayerEntity synchronizedPlayer) {
        boolean remnant = ((DissolutionPlayer) synchronizedPlayer).isRemnant();
        boolean incorporeal = remnant && ((DissolutionPlayer)synchronizedPlayer).getRemnantState().isSoul();
        UUID playerUuid = synchronizedPlayer.getUuid();
        return createCorporealityMessage(playerUuid, remnant, incorporeal);
    }

    @Contract(pure = true)
    private static CustomPayloadS2CPacket createCorporealityMessage(UUID playerUuid, boolean remnant, boolean incorporeal) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeUuid(playerUuid);
        buf.writeBoolean(remnant);
        buf.writeBoolean(incorporeal);
        return new CustomPayloadS2CPacket(REMNANT_SYNC, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createPossessionMessage(UUID playerUuid, int possessedId) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeUuid(playerUuid);
        buf.writeInt(possessedId);
        return new CustomPayloadS2CPacket(POSSESSION_SYNC, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createEtherealAnimationMessage() {
        return new CustomPayloadS2CPacket(ETHEREAL_ANIMATION, createEmptyBuffer());
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

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createAnchorDeleteMessage(int anchorId) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeInt(anchorId);
        return new CustomPayloadS2CPacket(ANCHOR_REMOVE, buf);
    }

    @Contract(pure = true)
    public static PacketByteBuf createEmptyBuffer() {
        return new PacketByteBuf(buffer());
    }

    @Contract(pure = true)
    public static PacketByteBuf createPossessionRequestBuffer(Entity entity) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeInt(entity.getEntityId());
        return buf;
    }
}
