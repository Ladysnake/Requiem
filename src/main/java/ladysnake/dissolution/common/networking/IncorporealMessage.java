package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.api.IIncorporealHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class IncorporealMessage implements IMessage
  {
    long playerUUIDMost;
	long playerUUIDLeast;
    IIncorporealHandler.CorporealityStatus corporalityStatus;
    
    // this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
    public IncorporealMessage() {}
    
    public IncorporealMessage(long UUIDMost, long UUIDLeast, IIncorporealHandler.CorporealityStatus corporalityStatus)
    {
      this.playerUUIDMost = UUIDMost;
      this.playerUUIDLeast = UUIDLeast;
      
      this.corporalityStatus = corporalityStatus;
    }
    
    @Override
    public void fromBytes(ByteBuf buf)
    {
      // the order is important
      this.playerUUIDMost = buf.readLong();
      this.playerUUIDLeast = buf.readLong();
      this.corporalityStatus = IIncorporealHandler.CorporealityStatus.values()[buf.readByte()];
    }
    
    @Override
    public void toBytes(ByteBuf buf)
    {
      buf.writeLong(playerUUIDMost);
      buf.writeLong(playerUUIDLeast);
      buf.writeByte(corporalityStatus.ordinal());
    }
  }