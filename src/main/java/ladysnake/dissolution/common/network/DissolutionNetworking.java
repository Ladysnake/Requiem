package ladysnake.dissolution.common.network;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadServerPacket;
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

    public static void sendToServer(CustomPayloadServerPacket packet) {
        MinecraftClient.getInstance().player.networkHandler.sendPacket(packet);
    }

    public static void sendTo(ServerPlayerEntity player, CustomPayloadClientPacket packet) {
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(packet);
        }
    }

    public static void sendToAllTracking(Entity tracked, CustomPayloadClientPacket packet) {
        if (tracked.world instanceof ServerWorld) {
            ((ServerWorld) tracked.world).getEntityTracker().method_14079(tracked, packet);
        }
    }

    @Contract(pure = true)
    public static CustomPayloadClientPacket createCorporealityMessage(PlayerEntity synchronizedPlayer) {
        boolean remnant = ((DissolutionPlayer) synchronizedPlayer).isRemnant();
        boolean incorporeal = remnant && ((DissolutionPlayer)synchronizedPlayer).getRemnantState().isSoul();
        UUID playerUuid = synchronizedPlayer.getUuid();
        return createCorporealityMessage(playerUuid, remnant, incorporeal);
    }

    @Contract(pure = true)
    public static CustomPayloadClientPacket createCorporealityMessage(UUID playerUuid, boolean remnant, boolean incorporeal) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeUuid(playerUuid);
        buf.writeBoolean(remnant);
        buf.writeBoolean(incorporeal);
        return new CustomPayloadClientPacket(REMNANT_SYNC, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadClientPacket createPossessionMessage(UUID playerUuid, int possessedId) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeUuid(playerUuid);
        buf.writeInt(possessedId);
        return new CustomPayloadClientPacket(POSSESSION_SYNC, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadClientPacket createEtherealAnimationMessage() {
        return new CustomPayloadClientPacket(ETHEREAL_ANIMATION, new PacketByteBuf(buffer()));
    }

    @Contract(pure = true)
    public static CustomPayloadServerPacket createLeftClickMessage() {
        return new CustomPayloadServerPacket(LEFT_CLICK_AIR, new PacketByteBuf(buffer()));
    }

    @Contract(pure = true)
    public static CustomPayloadServerPacket createRightClickMessage() {
        return new CustomPayloadServerPacket(RIGHT_CLICK_AIR, new PacketByteBuf(buffer()));
    }

    @Contract(pure = true)
    public static CustomPayloadServerPacket createEtherealFractureMessage() {
        return new CustomPayloadServerPacket(ETHEREAL_FRACTURE, new PacketByteBuf(buffer()));
    }

    @Contract(pure = true)
    public static CustomPayloadServerPacket createPossessionRequestMessage(Entity entity) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeInt(entity.getEntityId());
        return new CustomPayloadServerPacket(POSSESSION_REQUEST, buf);
    }
}
