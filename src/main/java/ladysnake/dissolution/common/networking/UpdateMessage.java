package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class UpdateMessage implements IMessage {
    boolean isIncorporeal;

    // this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
    public UpdateMessage() {
    }

    public UpdateMessage(boolean isIncorporeal) {
        this.isIncorporeal = isIncorporeal;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // the order is important
        this.isIncorporeal = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isIncorporeal);
    }
}