package ladysnake.dissolution.common.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class CommandDissolutionTreeBase extends CommandTreeBase {

    @Override
    public int getRequiredPermissionLevel() {
        return this.getSubCommands().stream().mapToInt(command -> ((CommandBase)command).getRequiredPermissionLevel()).min().orElse(4);
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
    {
        if(args.length < 1) {
            sender.sendMessage(new TextComponentString(CommandBase.joinNiceStringFromCollection(getCommandMap().keySet())));
        }
        else {
            ICommand cmd = getCommandMap().get(args[0]);

            if(cmd == null) {
                Optional<ICommand> command = getSubCommands().stream().filter(iCommand -> iCommand.getAliases().contains(args[0])).findAny();
                if(command.isPresent())
                    cmd = command.get();
                else
                    throw new CommandException("commands.tree_base.invalid_cmd", args[0]);
            }
            if(!cmd.checkPermission(server, sender)) {
                throw new CommandException("commands.generic.permission");
            }
            cmd.execute(server, sender, shiftArgs(args));
        }
    }

    protected static String[] shiftArgs(String[] s) {
        if(s == null || s.length == 0) {
            return new String[0];
        }

        String[] s1 = new String[s.length - 1];
        System.arraycopy(s, 1, s1, 0, s1.length);
        return s1;
    }

}
