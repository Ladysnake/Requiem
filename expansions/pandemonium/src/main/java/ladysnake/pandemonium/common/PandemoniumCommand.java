package ladysnake.pandemonium.common;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import ladysnake.pandemonium.api.event.PlayerShellEvents;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.function.Consumer;

import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.command.argument.GameProfileArgumentType.gameProfile;
import static net.minecraft.command.argument.GameProfileArgumentType.getProfileArgument;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class PandemoniumCommand {
    public static final String PANDEMONIUM_ROOT_COMMAND = "pandemonium";
    public static final String SHELL_SUBCOMMAND = "shell";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(PANDEMONIUM_ROOT_COMMAND)
            .requires(s -> s.hasPermissionLevel(2))
            .then(literal(SHELL_SUBCOMMAND)
                .then(literal("create")
                    .executes(context -> runOne(context.getSource().getPlayer(), player -> createShell(context.getSource().getPosition(), player)))
                    // pandemonium shell create
                    .then(argument("player", player())
                        // pandemonium shell create <player>
                        .executes(context -> runOne(getPlayer(context, "player"), player -> createShell(context.getSource().getPosition(), player)))
                    )
                )
                .then(literal("split")
                    // pandemonium shell split
                    .executes(context -> runOne(context.getSource().getPlayer(), PandemoniumCommand::split))
                    .then(argument("players", players())
                        // pandemonium shell split <players>
                        .executes(context -> runMany(getPlayers(context, "players"), PandemoniumCommand::split))
                    )
                )
                .then(literal("identity").then(literal("set")
                        .then(argument("shells", entities()).then(argument("profile", gameProfile())
                            .executes(context -> {
                                Collection<GameProfile> profiles = getProfileArgument(context, "profile");
                                if (profiles.size() > 1) throw TOO_MANY_PLAYERS_EXCEPTION.create();
                                GameProfile profile = profiles.iterator().next();
                                return runMany(getEntities(context, "shells"), s -> setIdentity(s, profile));
                            })
                        ))
                    )
                )
            )
        );
    }

    private static void setIdentity(Entity shell, GameProfile profile) {
        if (!(shell instanceof PlayerShellEntity)) {
            throw new CommandException(new TranslatableText("pandemonium.commands.shell.fail.not_shell"));
        }
        ((PlayerShellEntity) shell).setDisplayProfile(profile);
    }

    private static void createShell(Vec3d position, ServerPlayerEntity player) {
        PlayerShellEntity shell = PlayerSplitter.createShell(player);
        PlayerShellEvents.PLAYER_SPLIT.invoker().onPlayerSplit(player, player, shell);
        shell.updatePosition(position.x, position.y, position.z);
        player.world.spawnEntity(shell);
    }

    private static void split(ServerPlayerEntity player) {
        if (!RemnantComponent.get(player).getRemnantType().isDemon()) {
            throw new CommandException(new TranslatableText("pandemonium:commands.shell.split.fail.mortal", player.getDisplayName()));
        }
        if (RemnantComponent.get(player).isVagrant()) {
            throw new CommandException(new TranslatableText("pandemonium:commands.shell.split.fail.vagrant", player.getDisplayName()));
        }
        PlayerSplitter.split(player);
    }

    private static <T> int runOne(T element, Consumer<T> action) {
        action.accept(element);
        return 1;
    }

    private static <T> int runMany(Collection<T> collection, Consumer<T> action) {
        int count = 0;
        for (T element : collection) {
            action.accept(element);
            count++;
        }
        return count;
    }
}
