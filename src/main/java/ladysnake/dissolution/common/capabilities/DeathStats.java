package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.IDeathStats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class DeathStats implements IDeathStats {
    private boolean wasDead;
    private int deathDim;
    private Vec3d deathLoc = Vec3d.ZERO;
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
    public Vec3d getDeathLocation() {
        return deathLoc;
    }

    @Override
    public void setDeathLocation(Vec3d deathLoc) {
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
        nbt.setDouble("deathLocX", deathLoc.x);
        nbt.setDouble("deathLocY", deathLoc.y);
        nbt.setDouble("deathLocZ", deathLoc.y);
        nbt.setString("deathMsg", lastDeathMessage);
        nbt.setBoolean("wasDead", wasDead);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.deathLoc = new Vec3d(nbt.getDouble("deathLocX"), nbt.getDouble("deathLocY"), nbt.getDouble("deathLocZ"));
        this.lastDeathMessage = nbt.getString("deathMsg");
        this.wasDead = nbt.getBoolean("wasDead");
    }
}
