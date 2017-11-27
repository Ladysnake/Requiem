package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ConfigPacket implements IMessageHandler<ConfigMessage, IMessage> {
    @Override
    public IMessage onMessage(ConfigMessage message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> syncConfig(message));
        return null;
    }

    public void syncConfig(ConfigMessage message) {
        Dissolution.LOGGER.info("Synchronizing config with the server's");
        message.toSync.forEach((key, value) -> {
            if (DissolutionConfigManager.syncedProps.containsKey(key)) {
                Property prop = DissolutionConfigManager.syncedProps.get(key);
                DissolutionConfigManager.backupProps.put(prop, prop.getString());
                Dissolution.LOGGER.info(prop.getString() + " -> " + value);
                prop.set(value);
                prop.setRequiresWorldRestart(true);
            }
        });
        DissolutionConfigManager.load();
    }
}
