package ladysnake.dissolution.common.commands;

import net.minecraft.command.ICommandSender;

import javax.annotation.Nonnull;

public class CommandDialogueTree extends CommandDissolutionTreeBase {

    public CommandDialogueTree() {
        super();
        this.addSubcommand(new CommandDialogueReset());
        this.addSubcommand(new CommandDialogueSay());
    }

    @Nonnull
    @Override
    public String getName() {
        return "dialogue";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.dissolution.dialogue.usage";
    }

}
