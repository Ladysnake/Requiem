package ladysnake.requiem.mixin.command;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public abstract class EntitySelectorOptionsMixin {
    @Shadow
    private static native void putOption(String id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description);

    @Inject(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/command/EntitySelectorOptions;putOption(Ljava/lang/String;Lnet/minecraft/command/EntitySelectorOptions$SelectorHandler;Ljava/util/function/Predicate;Lnet/minecraft/text/Text;)V",
            ordinal = 20,
            shift = At.Shift.AFTER
        )
    )
    private static void registerRequiemSelectorOptions(CallbackInfo ci) {
        putOption("requiem:possessor", reader -> {
            boolean negated = reader.readNegationCharacter();
            String expectedName = reader.getReader().readString();
            reader.setPredicate((entity) -> {
                if (!(entity instanceof Possessable)) {
                    return false;
                } else {
                    PlayerEntity possessor = ((Possessable) entity).getPossessor();
                    String possessorName = possessor == null ? "" : possessor.getName().asString();
                    return possessorName.equals(expectedName) != negated;
                }
            });
        }, (reader) -> true, new TranslatableText("requiem:argument.entity.options.possessor.description"));
    }

    @ModifyArg(method = "suggestOptions", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;suggest(Ljava/lang/String;Lcom/mojang/brigadier/Message;)Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;"))
    private static String suggestQuotes(String text) {
        if (text.indexOf(':') > 0 && text.indexOf('"') < 0 && text.indexOf('=') == text.length() - 1) {
            return '"' + text.substring(0, text.length() -1) + "\"=";
        }
        return text;
    }
}
