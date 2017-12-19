package ladysnake.dissolution.common.tileentities;

import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import ladysnake.dissolution.common.entity.SoulType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo", striprefs = true)
public class TileEntityWispInAJar extends TileEntity implements ILightProvider {

    private SoulType containedSoul = SoulType.WILL_O_WISP;

    public SoulType getContainedSoul() {
        return containedSoul;
    }

    public void setContainedSoul(SoulType containedSoul) {
        this.containedSoul = containedSoul;
        this.markDirty();
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }

    @Override
    public Light provideLight() {
        return Light.builder().pos(this.pos).radius(5).color(0.5f, 0.5f, 0.8f).build();
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("soulType", getContainedSoul().toString());
        return new SPacketUpdateTileEntity(this.pos, 0, compound);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound compound = pkt.getNbtCompound();
        try {
            setContainedSoul(SoulType.valueOf(compound.getString("soulType")));
        } catch (IllegalArgumentException e) {
            setContainedSoul(SoulType.NONE);
            e.printStackTrace();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        try {
            setContainedSoul(SoulType.valueOf(compound.getString("soulType")));
        } catch (IllegalArgumentException e) {
            setContainedSoul(SoulType.NONE);
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("soulType", getContainedSoul().name());
        return compound;
    }

}
