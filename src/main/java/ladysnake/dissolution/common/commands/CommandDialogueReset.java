package ladysnake.dissolution.common.commands;

import ladysnake.dissolution.api.IDialogueStats;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

public class CommandDialogueReset extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "reset";
    }

    @Nonnull
    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.dissolution.dialogue.reset.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        EntityPlayer player = args.length >= 1 ? getPlayer(server, sender, args[0]) : getCommandSenderAsPlayer(sender);
        IDialogueStats handler = CapabilityIncorporealHandler.getHandler(player).getDialogueStats();
        handler.resetProgress();
        notifyCommandListener(sender, this, "commands.dissolution.dialogue.reset", player.getName());
    }

}
