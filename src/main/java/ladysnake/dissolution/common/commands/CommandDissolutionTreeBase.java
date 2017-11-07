package ladysnake.dissolution.common.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class CommandDissolutionTreeBase extends CommandTreeBase {

    @Override
    public int getRequiredPermissionLevel() {
        return this.getSubCommands().stream().mapToInt(command -> ((CommandBase)command).getRequiredPermissionLevel()).min().orElse(4);
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return this.getSubCommands().stream().anyMatch(command -> command.checkPermission(server, sender));
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos pos) {
        List<String> ret = super.getTabCompletions(server, sender, args, pos);
        if(ret.isEmpty() && args.length == 1) {
            ret = getListOfStringsMatchingLastWord(args, getSubCommands().stream()
                    .filter(command -> command.checkPermission(server, sender))
                    .flatMap(command -> command.getAliases().stream())
                    .sorted().collect(Collectors.toList()));
        } else if(ret.isEmpty()) {
            Optional<ICommand> cmd = this.getSubCommands().stream().filter(command -> command.getAliases().contains(args[0])).findAny();
            if(cmd.isPresent())
                return cmd.get().getTabCompletions(server, sender, shiftArgs(args), pos);
        }
        return ret;
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
