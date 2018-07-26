package ladysnake.dissolution.common.commands;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandSoulStrength extends CommandBase {
    private List<String> strongAliases = Arrays.asList("true", "strong", "stronk", "worthy", "determined");
    private List<String> weakAliases = Arrays.asList("false", "weak", "unworthy");

    @Nonnull
    @Override
    public String getName() {
        return "setRemnant";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("soul");
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.dissolution.soul_strength.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> possibilities = null;
        if (args.length == 1) {
            possibilities = Arrays.asList(server.getOnlinePlayerNames());
        } else if (args.length == 2) {
            possibilities = Arrays.asList("true", "false");
        }
        return possibilities == null ? Collections.EMPTY_LIST : getListOfStringsMatchingLastWord(args, possibilities);
    }

    /**
     * usage : /dissolution soul <set|query> [strong|weak] [player]
     */
    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("commands.dissolution.soul_strength.usage");
        }
        EntityPlayer player = getPlayer(server, sender, args[0]);
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
        if (args.length == 1) {
            sender.sendMessage(
                    new TextComponentTranslation("commands.dissolution.soul_strength.get." + (player == sender ? "self" : "other"), player.getName(),
                            new TextComponentTranslation("dissolution.soul_strength." + (handler.isStrongSoul() ? "strong" : "weak"))));
        } else {
            if (Dissolution.config.forceRemnant != DissolutionConfigManager.EnforcedSoulStrength.DEFAULT) {
                throw new CommandException("commands.dissolution.soul_strength.enforced_soul_strength");
            }
            TextComponentTranslation soulStrength;
            if (strongAliases.contains(args[1])) {
                handler.setStrongSoul(true);
                soulStrength = new TextComponentTranslation("dissolution.soul_strength.strong");
            } else if (weakAliases.contains(args[1])) {
                handler.setStrongSoul(false);
                soulStrength = new TextComponentTranslation("dissolution.soul_strength.weak");
            } else {
                throw new WrongUsageException("commands.dissolution.soul_strength.set.usage");
            }
            notifyCommandListener(sender, this, "commands.dissolution.soul_strength.update", player.getName());
            if (sender.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.sendMessage(new TextComponentTranslation("dissolution.soul_strength.changed", soulStrength));
            }
        }
    }
}
