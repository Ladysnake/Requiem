package ladysnake.tartaros.common.networking;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PingMessage implements IMessage
  {
    
    // this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
    public PingMessage() {}
    
    @Override
    public void fromBytes(ByteBuf buf)
    { }
    
    @Override
    public void toBytes(ByteBuf buf)
    { }
  }