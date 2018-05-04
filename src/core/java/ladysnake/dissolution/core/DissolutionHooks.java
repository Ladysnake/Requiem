package ladysnake.dissolution.core;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import java.util.Arrays;

public class DissolutionHooks {
    @CapabilityInject(IIncorporealHandler.class)
    public static Capability<IIncorporealHandler> cap;

    @SuppressWarnings("unused") // called by ASM voodoo magic
    public static EntityLivingBase getPossessedEntity(Entity player) {
        if (player.hasCapability(cap, null)) {
            IPossessable possessable = player.getCapability(cap, null).getPossessed();
            if (possessable instanceof EntityLivingBase)
                return (EntityLivingBase) possessable;
        }
        return null;
    }

    public static void test() {
        print(1, "", "");
        print(new Object[] {""});
    }

    public static void print(Object arg) {
        System.out.println(arg);
    }
    public static void print(Object... arg) {
        System.out.println(Arrays.toString(arg));
    }
    public static void print(boolean arg) {
        System.out.println(arg);
    }
    public static void print(byte arg) {
        System.out.println(arg);
    }
    public static void print(char arg) {
        System.out.println(arg);
    }
    public static void print(int arg) {
        System.out.println(arg);
    }
    public static void print(float arg) {
        System.out.println(arg);
    }
    public static void print(long arg) {
        System.out.println(arg);
    }
    public static void print(double arg) {
        System.out.println(arg);
    }
}
