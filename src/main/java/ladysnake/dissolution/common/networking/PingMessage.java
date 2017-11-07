package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PingMessage implements IMessage
  {
	long uuidMost, uuidLeast;
    
    // this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
    public PingMessage() {}
    
    public PingMessage(long uuidMost, long uuidLeast) {
    	this.uuidMost = uuidMost;
    	this.uuidLeast = uuidLeast;
    }
    
    @Override
    public void fromBytes(ByteBuf buf)
    {
    	this.uuidMost = buf.readLong();
    	this.uuidLeast = buf.readLong();
    }
    
    @Override
    public void toBytes(ByteBuf buf)
    {
    	buf.writeLong(this.uuidMost);
    	buf.writeLong(this.uuidLeast);
    }
  }