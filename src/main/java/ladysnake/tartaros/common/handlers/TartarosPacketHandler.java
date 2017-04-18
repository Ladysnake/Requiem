package ladysnake.tartaros.common.handlers;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.capabilities.IncorporealMessage;
import ladysnake.tartaros.common.networkingtest.SimplePacket;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class TartarosPacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID.toUpperCase());
	  
	  public static void initPackets()
	  {
	    registerMessage(IncorporealMessage.IncorporealMessageHandler.class, IncorporealMessage.class);
	  }
	  
	  private static int nextPacketId = 0;
	  
	  private static void registerMessage(Class packet, Class message)
	  {
	    INSTANCE.registerMessage(packet, message, nextPacketId, Side.CLIENT);
	    INSTANCE.registerMessage(packet, message, nextPacketId, Side.SERVER);
	    nextPacketId++;
	  }
	
}
