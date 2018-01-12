package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.IDialogueStats;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.LinkedList;
import java.util.List;

import static ladysnake.dissolution.common.config.DissolutionConfigManager.EnforcedSoulStrength.NONE;

public class DialogueStats implements IDialogueStats {
    private boolean hasBeenContacted;
    private List<String> history;
    private CapabilityIncorporealHandler.DefaultIncorporealHandler capability;

    public DialogueStats(CapabilityIncorporealHandler.DefaultIncorporealHandler capability) {
        this.capability = capability;
        this.history = new LinkedList<>();
    }

    @Override
    public void checkFirstConnection() {
        if (Dissolution.config.enforcedSoulStrength == NONE && !hasBeenContacted && !capability.getOwner().world.isRemote) {
            sendNextDialogue("dissolution.dialogues.first_contact.header", "dissolution.dialogues.first_contact", 2);
            hasBeenContacted = true;
        }
    }

    @Override
    public void updateDialogue(int choice) {
        if (hasBeenContacted && history.isEmpty()) {
            boolean strongSoul = choice == 0;
            if (!strongSoul && capability.getCorporealityStatus().isIncorporeal())
                capability.setCorporealityStatus(SoulStates.BODY);
            capability.setStrongSoul(strongSoul);
            history.add(strongSoul ? "strong soul" : "weak soul");
            capability.getOwner().sendMessage(new TextComponentTranslation("dissolution.dialogues.choose_soul_strength").setStyle(new Style().setColor(TextFormatting.RED)));
            capability.getOwner().sendMessage(new TextComponentTranslation("dissolution.soul_strength.changed",
                    new TextComponentTranslation("dissolution.soul_strength." + (capability.isStrongSoul() ? "strong" : "weak"))));
        }
    }

    protected void sendNextDialogue(String announcement, String npcLine, int choiceCount) {
        String[] choices = new String[choiceCount];
        if (Dissolution.config.technicianDialogue) npcLine += ".explicit";
        for (int i = 0; i < choiceCount; i++)
            choices[i] = npcLine + ".choice_" + i;
        if (announcement != null) {
            if (Dissolution.config.technicianDialogue) announcement += ".explicit";
            ITextComponent headerComponent = new TextComponentTranslation(announcement);
            headerComponent.getStyle().setColor(TextFormatting.WHITE);
            headerComponent.getStyle().setItalic(true);
            capability.getOwner().sendMessage(headerComponent);
        }
        ITextComponent npcComponent = new TextComponentTranslation(npcLine);
        npcComponent.getStyle().setColor(TextFormatting.RED);
        for (ITextComponent chatMessage : generateDialogue(npcComponent, choices))
            capability.getOwner().sendMessage(chatMessage);
    }

    protected static List<ITextComponent> generateDialogue(ITextComponent npcLine, String... choices) {
        List<ITextComponent> components = new LinkedList<>();
        components.add(npcLine);
        for (int i = 0; i < choices.length; i++) {
            TextComponentTranslation choice = new TextComponentTranslation(choices[i]);
            TextComponentTranslation choiceDialogue = new TextComponentTranslation("dissolution.dialogues.choice", i, choice);
            Style style = new Style();
            style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dissolution dialogue say " + i));
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("dissolution.dialogues.choice.hover")));
            style.setColor(TextFormatting.GRAY);
            choiceDialogue.setStyle(style);
            components.add(choiceDialogue);
        }
        ITextComponent hint = new TextComponentTranslation("dissolution.dialogues.choice.hint");
        hint.getStyle().setItalic(true);
        hint.getStyle().setColor(TextFormatting.DARK_GRAY);
        components.add(hint);
        return components;
    }

    @Override
    public void resetProgress() {
        hasBeenContacted = false;
        history.clear();
        checkFirstConnection();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("contacted", hasBeenContacted);
        NBTTagList nbtHistory = new NBTTagList();
        for (String s : this.history)
            nbtHistory.appendTag(new NBTTagString(s));
        nbt.setTag("history", nbtHistory);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.hasBeenContacted = nbt.getBoolean("contacted");
        NBTTagList nbtHistory = nbt.getTagList("history", 8);
        for (NBTBase nbtBase : nbtHistory)
            history.add(((NBTTagString) nbtBase).getString());
    }
}
