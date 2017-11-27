package ladysnake.dissolution.common.registries.modularsetups;

import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Set;

public interface ISetupInstance {

    /**
     * Called each tick when this setup is active in the modular machine
     */
    void onTick();

    /**
     * Called when a player interacts with the {@link BlockCasing} hosting the tile entity
     *
     * @param playerIn the player interacting with the casing
     * @param hand     the hand used to interact
     * @param part     the part of the {@link BlockCasing} that the player interacted with
     * @param facing   the face of the {@link BlockCasing} that the player interacted with
     * @param hitX     the precise x coordinate of the interaction on the block's surface
     * @param hitY     the precise y coordinate of the interaction on the block's surface
     * @param hitZ     the precise z coordinate of the interaction on the block's surface
     */
    default void onInteract(EntityPlayer playerIn, EnumHand hand, BlockCasing.EnumPartType part, EnumFacing facing, float hitX, float hitY, float hitZ) {
    }

    default void init() {
    }

    /**
     * Called when the setup is invalidated
     */
    default void onRemoval() {
    }

    default void addModelsForRender(Set<ResourceLocation> models) {
    }

    /**
     * @return a custom model location for the plug on this face or null if the default should be used
     */
    default ResourceLocation getPlugModel(EnumFacing facing, BlockCasing.EnumPartType part, ResourceLocation defaultModel) {
        return defaultModel;
    }

    /**
     * Loads the extra information of this setup from the save
     *
     * @param compound the compound to be read
     */
    default void readFromNBT(NBTTagCompound compound) {
    }

    /**
     * Stores this setup's extra information (inventory, power, state, etc.)
     *
     * @param compound a NBT compound to write the information on
     * @return the compound with stored information in it
     */
    default NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return compound;
    }

    /**
     * see {@link #getCapability}
     */
    boolean hasCapability(Capability<?> capability, EnumFacing facing, BlockCasing.EnumPartType part);

    /**
     * Proxy method that allows this setup to give capabilities to the machine
     *
     * @param capability the capability being checked
     * @param facing     the face being checked, <em>compensated</em> with the machine's rotation
     * @param part       the part of the {@link BlockCasing} from which the method was called
     * @return an instance of this capability or null if it is not possessed by this setup
     */
    @Nullable
    <T> T getCapability(Capability<T> capability, EnumFacing facing, BlockCasing.EnumPartType part);

}
