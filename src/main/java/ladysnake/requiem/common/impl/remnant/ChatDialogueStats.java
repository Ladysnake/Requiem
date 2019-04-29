package ladysnake.requiem.common.impl.remnant;

import ladysnake.requiem.api.v1.player.DialogueTracker;
import ladysnake.requiem.api.v1.player.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.text.event.ClickEvent;
import net.minecraft.text.event.HoverEvent;

import java.util.LinkedList;
import java.util.List;

public class ChatDialogueStats implements DialogueTracker {
    private boolean hasBeenContacted;
    private List<String> history;
    private PlayerEntity owner;
    private static final boolean TECHNICIAN_DIALOGUE = false;

    public ChatDialogueStats(PlayerEntity owner) {
        this.owner = owner;
        this.history = new LinkedList<>();
    }

    @Override
    public void checkFirstConnection() {
        if (!hasBeenContacted && !this.owner.world.isClient) {
            sendNextDialogue("dissolution.dialogues.first_contact.header", "dissolution.dialogues.first_contact", 2);
            hasBeenContacted = true;
        }
    }

    @Override
    public void updateDialogue(int choice) {
        if (hasBeenContacted && history.isEmpty()) {
            boolean strongSoul = choice == 0;
            RemnantState remnantState = ((RequiemPlayer) this.owner).getRemnantState();
            if (!strongSoul && remnantState.isSoul()) {
                remnantState.setSoul(false);
            }
            ((RequiemPlayer) this.owner).setRemnant(strongSoul);
            history.add(strongSoul ? "strong soul" : "weak soul");
            this.owner.sendMessage(new TranslatableTextComponent("dissolution.dialogues.choose_soul_strength").setStyle(new Style().setColor(TextFormat.RED)));
            this.owner.sendMessage(new TranslatableTextComponent("dissolution.soul_strength.changed",
                    new TranslatableTextComponent("dissolution.soul_strength." + (((RequiemPlayer)this.owner).isRemnant() ? "strong" : "weak"))));
        }
    }

    protected void sendNextDialogue(String announcement, String npcLine, int choiceCount) {
        String[] choices = new String[choiceCount];
        if (TECHNICIAN_DIALOGUE) {
            npcLine += ".explicit";
        }
        for (int i = 0; i < choiceCount; i++) {
            choices[i] = npcLine + ".choice_" + i;
        }
        if (announcement != null) {
            if (TECHNICIAN_DIALOGUE) {
                announcement += ".explicit";
            }
            TextComponent headerComponent = new TranslatableTextComponent(announcement);
            headerComponent.getStyle().setColor(TextFormat.WHITE);
            headerComponent.getStyle().setItalic(true);
            this.owner.sendMessage(headerComponent);
        }
        TextComponent npcComponent = new TranslatableTextComponent(npcLine);
        npcComponent.getStyle().setColor(TextFormat.RED);
        for (TextComponent chatMessage : generateDialogue(npcComponent, choices)) {
            this.owner.sendMessage(chatMessage);
        }
    }

    protected static List<TextComponent> generateDialogue(TextComponent npcLine, String... choices) {
        List<TextComponent> components = new LinkedList<>();
        components.add(npcLine);
        for (int i = 0; i < choices.length; i++) {
            TranslatableTextComponent choice = new TranslatableTextComponent(choices[i]);
            TranslatableTextComponent choiceDialogue = new TranslatableTextComponent("dissolution.dialogues.choice", i, choice);
            Style style = new Style();
            style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dissolution dialogue say " + i));
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableTextComponent("dissolution.dialogues.choice.hover")));
            style.setColor(TextFormat.GRAY);
            choiceDialogue.setStyle(style);
            components.add(choiceDialogue);
        }
        TextComponent hint = new TranslatableTextComponent("dissolution.dialogues.choice.hint");
        hint.getStyle().setItalic(true);
        hint.getStyle().setColor(TextFormat.DARK_GRAY);
        components.add(hint);
        return components;
    }

    @Override
    public void resetProgress() {
        hasBeenContacted = false;
        history.clear();
        checkFirstConnection();
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("contacted", hasBeenContacted);
        ListTag nbtHistory = new ListTag();
        for (String s : this.history) {
            nbtHistory.add(new StringTag(s));
        }
        nbt.put("history", nbtHistory);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.hasBeenContacted = nbt.getBoolean("contacted");
        ListTag nbtHistory = nbt.getList("history", 8);
        for (Tag nbtBase : nbtHistory) {
            history.add(nbtBase.asString());
        }
    }
}
