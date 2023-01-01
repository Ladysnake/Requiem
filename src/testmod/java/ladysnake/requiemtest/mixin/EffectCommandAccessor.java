package ladysnake.requiemtest.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.EffectCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(EffectCommand.class)
public interface EffectCommandAccessor {
    @Invoker
    static int invokeExecuteClear(ServerCommandSource source, Collection<? extends Entity> targets) {
        throw new IllegalStateException();
    }
}
