package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.IDialogueStats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;

import java.util.LinkedList;
import java.util.List;

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
        if(!hasBeenContacted && !capability.getOwner().world.isRemote) {
            sendNextDialogue("dissolution.dialogues.first_contact", 2);
            hasBeenContacted = true;
        }
    }

    @Override
    public void updateDialogue(int choice) {
        if(hasBeenContacted && history.isEmpty()) {
            capability.setStrongSoul(choice == 0);
            history.add(choice == 0 ? "weak soul" : "strong soul");
            capability.getOwner().sendMessage(new TextComponentTranslation("dissolution.dialogues.choose_soul_strength"));
            capability.getOwner().sendMessage(new TextComponentTranslation("dissolution.soul_strength.changed",
                    new TextComponentTranslation("dissolution.soul_strength." + (capability.isStrongSoul() ? "strong" : "weak"))));
        }
    }

    protected void sendNextDialogue(String npcLine, int choiceCount) {
        String[] choices = new String[choiceCount];
        for(int i = 0; i < choiceCount; i++)
            choices[i] = npcLine + ".choice_" + i;
        for(ITextComponent chatMessage : generateDialogue(new TextComponentTranslation(npcLine), choices))
            capability.getOwner().sendMessage(chatMessage);
    }

    protected static List<ITextComponent> generateDialogue(ITextComponent npcLine, String... choices) {
        List<ITextComponent> components = new LinkedList<>();
        components.add(npcLine);
        for(int i = 0; i < choices.length; i++) {
            TextComponentTranslation choice = new TextComponentTranslation(choices[i]);
            TextComponentTranslation choiceDialogue = new TextComponentTranslation("dissolution.dialogues.choice", i, choice);
            Style style = new Style();
            style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dissolution dialogue say " + i));
            choiceDialogue.setStyle(style);
            components.add(choiceDialogue);
        }
        return components;
    }

    @Override
    public void resetProgress() {
        hasBeenContacted = false;
        checkFirstConnection();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("contacted", hasBeenContacted);
        NBTTagList nbtHistory = new NBTTagList();
        for(String s : this.history)
            nbtHistory.appendTag(new NBTTagString(s));
        nbt.setTag("history", nbtHistory);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.hasBeenContacted = nbt.getBoolean("contacted");
        NBTTagList nbtHistory = nbt.getTagList("history", 8);
        for(NBTBase nbtBase : nbtHistory)
            history.add(((NBTTagString)nbtBase).getString());
    }
}
