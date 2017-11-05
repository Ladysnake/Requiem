package ladysnake.dissolution.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IDialogueStats extends INBTSerializable<NBTTagCompound> {

    void checkFirstConnection();

    void updateDialogue(int choice);

    void resetProgress();
}
