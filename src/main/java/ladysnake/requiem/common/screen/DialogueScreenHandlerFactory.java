package ladysnake.requiem.common.screen;

import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class DialogueScreenHandlerFactory implements ExtendedScreenHandlerFactory {
    private final CutsceneDialogue dialogue;
    private final Text displayName;

    public DialogueScreenHandlerFactory(CutsceneDialogue dialogue, Text displayName) {
        this.dialogue = dialogue;
        this.displayName = displayName;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeIdentifier(this.dialogue.getId());
        buf.writeString(this.dialogue.getCurrentStateKey());
    }

    @Override
    public Text getDisplayName() {
        return this.displayName;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new DialogueScreenHandler(RequiemScreenHandlers.DIALOGUE_SCREEN_HANDLER, syncId, this.dialogue);
    }
}
