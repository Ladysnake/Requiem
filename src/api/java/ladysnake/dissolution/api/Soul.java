package ladysnake.dissolution.api;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Random;

public class Soul {

    public static final Soul UNDEFINED = new Soul(SoulTypes.UNTYPED, 0, 0);

    private static final Random rand = new Random();
    private static int mean = 40, deviation = 10;

    private final SoulTypes type;
    private int purity;
    private int willingness;

    public Soul(SoulTypes type) {
        this(type, (int) Math.round(Math.abs((rand.nextGaussian() * deviation) + mean)) % 100, (int) Math.round(Math.abs((rand.nextGaussian() * deviation) + mean)) % 100);
    }

    public Soul(SoulTypes type, int purity, int willingness) {
        super();
        this.type = type;
        this.purity = purity;
        this.willingness = willingness;
    }

    public Soul(NBTTagCompound nbt) {
        this(SoulTypes.valueOf(nbt.getString("soulType")), nbt.getInteger("purity"), nbt.getInteger("familiarity"));
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("soulType", type.name());
        nbt.setInteger("purity", purity);
        nbt.setInteger("familiarity", willingness);
        return nbt;
    }

    public SoulTypes getType() {
        return type;
    }

    public int getPurity() {
        return purity;
    }

    public int getWillingness() {
        return willingness;
    }

    public void setWillingness(int willingness) {
        this.willingness = willingness;
    }

    @Override
    public String toString() {
        return "Soul [type=" + type + ", purity=" + purity + ", willingness=" + willingness + "]";
    }

}
