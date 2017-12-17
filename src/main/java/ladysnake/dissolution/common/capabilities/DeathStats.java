package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.corporeality.IDeathStats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class DeathStats implements IDeathStats {
    private boolean wasDead;
    private int deathDim;
    private BlockPos deathLoc = BlockPos.ORIGIN;
    private String lastDeathMessage = "";

    @Override
    public void setDead(boolean dead) {
        wasDead = dead;
    }

    @Override
    public boolean wasDead() {
        return wasDead;
    }

    @Override
    public int getDeathDimension() {
        return deathDim;
    }

    @Override
    public void setDeathDimension(int dimension) {
        this.deathDim = dimension;
    }

    @Override
    public BlockPos getDeathLocation() {
        return deathLoc;
    }

    @Override
    public void setDeathLocation(BlockPos deathLoc) {
        this.deathLoc = deathLoc;
    }

    @Override
    public String getLastDeathMessage() {
        return this.lastDeathMessage;
    }

    @Override
    public void setLastDeathMessage(String deathMessage) {
        this.lastDeathMessage = deathMessage;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("deathLoc", deathLoc.toLong());
        nbt.setInteger("deathDim", deathDim);
        nbt.setString("deathMsg", lastDeathMessage);
        nbt.setBoolean("wasDead", wasDead);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.deathLoc = BlockPos.fromLong(nbt.getLong("deathLoc"));
        this.deathDim = nbt.getInteger("deathDim");
        this.lastDeathMessage = nbt.getString("deathMsg");
        this.wasDead = nbt.getBoolean("wasDead");
    }
}
