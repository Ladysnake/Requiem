package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class FlashTransitionMessage implements IMessage {
    boolean fadeIn;

    public FlashTransitionMessage() {
        super();
    }

    public FlashTransitionMessage(boolean fadeIn) {
        this.fadeIn = fadeIn;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.fadeIn = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(fadeIn);
    }
}
