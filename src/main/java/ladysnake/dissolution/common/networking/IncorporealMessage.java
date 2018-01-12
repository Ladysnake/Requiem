package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class IncorporealMessage implements IMessage {
    long playerUUIDMost;
    long playerUUIDLeast;
    boolean strongSoul;
    ICorporealityStatus corporealityStatus;

    // this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
    @SuppressWarnings("unused")
    public IncorporealMessage() {
    }

    public IncorporealMessage(long UUIDMost, long UUIDLeast, boolean strongSoul, ICorporealityStatus corporealityStatus) {
        this.playerUUIDMost = UUIDMost;
        this.playerUUIDLeast = UUIDLeast;
        this.strongSoul = strongSoul;
        this.corporealityStatus = corporealityStatus;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // the order is important
        this.playerUUIDMost = buf.readLong();
        this.playerUUIDLeast = buf.readLong();
        byte b = buf.readByte();
        this.strongSoul = (b & 0b1000_0000) > 0;
        this.corporealityStatus = SoulStates.REGISTRY.getValues().get(b & 0b0111_1111);    // yes I assume that there won't be more than 127 possible statuses
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(playerUUIDMost);
        buf.writeLong(playerUUIDLeast);
        int statusId = SoulStates.REGISTRY.getValues().indexOf(corporealityStatus);
        buf.writeByte(statusId | (strongSoul ? 0b1000_0000 : 0));
    }
}