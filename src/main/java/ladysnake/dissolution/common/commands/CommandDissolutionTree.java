package ladysnake.dissolution.common.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommandDissolutionTree extends CommandDissolutionTreeBase {

    public CommandDissolutionTree() {
        super();
        this.addSubcommand(new CommandCorporealMode());
        this.addSubcommand(new CommandSoulStrength());
        this.addSubcommand(new CommandDialogueTree());
        this.addSubcommand(new CommandStuck());
    }

    @Nonnull
    @Override
    public String getName() {
        return "dissolution";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("dis");
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "command.dissolution.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString(CommandBase.joinNiceStringFromCollection(getCommandMap().keySet())));
        } else {
            ICommand cmd = getCommandMap().get(args[0]);

            if (cmd == null) {
                Optional<ICommand> command = getSubCommands().stream().filter(iCommand -> iCommand.getAliases().contains(args[0])).findAny();
                if (command.isPresent())
                    cmd = command.get();
                else
                    throw new CommandException("commands.tree_base.invalid_cmd", args[0]);
            }
            if (!cmd.checkPermission(server, sender)) {
                throw new CommandException("commands.generic.permission");
            }
            cmd.execute(server, sender, shiftArgs(args));
        }
    }

    protected static String[] shiftArgs(String[] s) {
        if (s == null || s.length == 0) {
            return new String[0];
        }

        String[] s1 = new String[s.length - 1];
        System.arraycopy(s, 1, s1, 0, s1.length);
        return s1;
    }

}
