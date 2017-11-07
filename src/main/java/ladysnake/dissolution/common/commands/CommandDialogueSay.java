package ladysnake.dissolution.common.commands;

import ladysnake.dissolution.api.IDialogueStats;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

public class CommandDialogueSay extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "say";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.dissolution.dialogue.say.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayer;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if(args.length <= 0) throw new WrongUsageException(getUsage(sender));
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        IDialogueStats dialogueStats = CapabilityIncorporealHandler.getHandler(player).getDialogueStats();
        try {
            dialogueStats.updateDialogue(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            throw new WrongUsageException("commands.dissolution.dialogue.say.not_a_number", args[0]);
        }
    }
}
