package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class PossessionMessage implements IMessage {
    UUID playerUuid;
    int possessedUuid;

    public PossessionMessage() {
    }

    public PossessionMessage(UUID playerUuid, int possessedId) {
        this.playerUuid = playerUuid;
        this.possessedUuid = possessedId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buf2 = new PacketBuffer(buf);
        this.playerUuid = buf2.readUniqueId();
        this.possessedUuid = buf2.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buf2 = new PacketBuffer(buf);
        buf2.writeUniqueId(this.playerUuid);
        buf2.writeInt(this.possessedUuid);
    }
}
