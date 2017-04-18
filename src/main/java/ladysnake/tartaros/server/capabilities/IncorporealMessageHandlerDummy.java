package ladysnake.tartaros.server.capabilities;

import ladysnake.tartaros.common.capabilities.IncorporealMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class IncorporealMessageHandlerDummy implements IMessageHandler<IncorporealMessage, IMessage> {

	@Override
	public IMessage onMessage(IncorporealMessage message, MessageContext ctx) {
		System.out.println("there's an issue here.");
		return null;
	}

}
