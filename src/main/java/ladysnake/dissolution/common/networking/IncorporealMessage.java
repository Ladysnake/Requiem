package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class IncorporealMessage implements IMessage {
    int playerId;
    boolean strongSoul;
    ICorporealityStatus corporealityStatus;

    // this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
    @SuppressWarnings("unused")
    public IncorporealMessage() {
    }

    public IncorporealMessage(int playerId, boolean strongSoul, ICorporealityStatus corporealityStatus) {
        this.playerId = playerId;
        this.strongSoul = strongSoul;
        this.corporealityStatus = corporealityStatus;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // the order is important
        this.playerId = buf.readInt();
        byte b = buf.readByte();
        this.strongSoul = (b & 0b1000_0000) > 0;
        this.corporealityStatus = SoulStates.REGISTRY.getValues().get(b & 0b0111_1111);    // yes I assume that there won't be more than 127 possible statuses
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(playerId);
        int statusId = SoulStates.REGISTRY.getValues().indexOf(corporealityStatus);
        buf.writeByte(statusId | (strongSoul ? 0b1000_0000 : 0));
    }
}