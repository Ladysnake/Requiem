package ladysnake.dissolution.common.commands;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("SpellCheckingInspection")
public class CommandCorporealMode extends CommandBase {

    @Nonnull
    @Override
    public String getName() {
        return "corporeality_mode";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("cmode");
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.dissolution.corporeality_mode.usage";
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> possibilities = null;
        if(args.length == 1) {
            possibilities = Arrays.asList("query", "set");
        } else if (args.length == 2 && "set".equals(args[0])) {
            possibilities = Arrays.stream(IIncorporealHandler.CorporealityStatus.values()).map(IIncorporealHandler.CorporealityStatus::toString).collect(Collectors.toList());
        } else if(args.length > 1) {
            possibilities = Arrays.asList(server.getOnlinePlayerNames());
        }
        return possibilities == null ? Collections.EMPTY_LIST : getListOfStringsMatchingLastWord(args, possibilities);
    }

    /**
     * usage : /dissolution cmode <set|query> [mode] [player selector]
     */
    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if(args.length <= 0)
            throw new WrongUsageException("commands.dissolution.corporeality_mode.usage");
        if("query".equals(args[0]) || "get".equals(args[0])) {                 // dissolution cmode query  -> returns the player's current status
            EntityPlayer player = args.length >= 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
            notifyCommandListener(sender, this,
                    "commands.dissolution.corporeality_mode.query." + (player == sender ? "self" : "other"),     // different feedback
                    player.getName(), new TextComponentTranslation(handler.getCorporealityStatus().getUnlocalizedName()));
        } else if("set".equals(args[0])) {          // dissolution cmode set -> sets the player's status
            if(args.length == 1)
                throw new WrongUsageException("commands.dissolution.corporeality_mode.set.usage");       // have a more precise usage
            try {
                IIncorporealHandler.CorporealityStatus newStatus = IIncorporealHandler.CorporealityStatus.valueOf(args[1].toUpperCase());
                EntityPlayer player = args.length >= 3 ? getPlayer(server, sender, args[2]) : getCommandSenderAsPlayer(sender);
                IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
                if(!handler.isStrongSoul())
                    throw new CommandException("commands.dissolution.corporeality_mode.set.weak_soul",player.getName());
                handler.setCorporealityStatus(newStatus);
                ITextComponent iTextComponent = new TextComponentTranslation(newStatus.getUnlocalizedName());
                if (sender.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback"))
                    player.sendMessage(new TextComponentTranslation("dissolution.corporeality_mode.changed", iTextComponent));
                notifyCommandListener(sender, this, "commands.dissolution.corporeality_mode.success", player.getName());
            } catch (IllegalArgumentException e) {    // the status name is invalid
                throw new CommandException("commands.dissolution.corporeality_mode.no_such_status", args[1]);
            }
        } else throw new WrongUsageException("commands.dissolution.corporeality_mode.usage");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
