package ladysnake.dissolution.common.network;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
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
    public static final Identifier REMNANT_SYNC = Dissolution.id("remnant_sync");
    public static final Identifier POSSESSION_SYNC = Dissolution.id("possession_sync");
    public static final Identifier ETHEREAL_ANIMATION = Dissolution.id("ethereal_animation");

    // Client -> Server
    public static final Identifier LEFT_CLICK_AIR = Dissolution.id("attack_air");
    public static final Identifier RIGHT_CLICK_AIR = Dissolution.id("interact_air");
    public static final Identifier POSSESSION_REQUEST = Dissolution.id("possession_request");
    public static final Identifier ETHEREAL_FRACTURE = Dissolution.id("ethereal_fracture");

    public static void sendToServer(CustomPayloadC2SPacket packet) {
        MinecraftClient.getInstance().player.networkHandler.sendPacket(packet);
    }

    public static void sendTo(ServerPlayerEntity player, CustomPayloadS2CPacket packet) {
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(packet);
        }
    }

    public static void sendToAllTracking(Entity tracked, CustomPayloadS2CPacket packet) {
        if (tracked.world instanceof ServerWorld) {
            PlayerStream.watching(tracked).forEach(p -> sendTo((ServerPlayerEntity) p, packet));
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
    public static CustomPayloadS2CPacket createCorporealityMessage(UUID playerUuid, boolean remnant, boolean incorporeal) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeUuid(playerUuid);
        buf.writeBoolean(remnant);
        buf.writeBoolean(incorporeal);
        return new CustomPayloadS2CPacket(REMNANT_SYNC, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createPossessionMessage(UUID playerUuid, int possessedId) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeUuid(playerUuid);
        buf.writeInt(possessedId);
        return new CustomPayloadS2CPacket(POSSESSION_SYNC, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createEtherealAnimationMessage() {
        return new CustomPayloadS2CPacket(ETHEREAL_ANIMATION, new PacketByteBuf(buffer()));
    }

    @Contract(pure = true)
    public static CustomPayloadC2SPacket createLeftClickMessage() {
        return new CustomPayloadC2SPacket(LEFT_CLICK_AIR, new PacketByteBuf(buffer()));
    }

    @Contract(pure = true)
    public static CustomPayloadC2SPacket createRightClickMessage() {
        return new CustomPayloadC2SPacket(RIGHT_CLICK_AIR, new PacketByteBuf(buffer()));
    }

    @Contract(pure = true)
    public static CustomPayloadC2SPacket createEtherealFractureMessage() {
        return new CustomPayloadC2SPacket(ETHEREAL_FRACTURE, new PacketByteBuf(buffer()));
    }

    @Contract(pure = true)
    public static CustomPayloadC2SPacket createPossessionRequestMessage(Entity entity) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeInt(entity.getEntityId());
        return new CustomPayloadC2SPacket(POSSESSION_REQUEST, buf);
    }
}
