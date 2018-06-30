package ladysnake.dissolution.common.compat;

import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class ThaumcraftCompat {

    public static void assignAspects() {
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.HUMAN_FLESH_RAW), new AspectList()
                .add(Aspect.MAN, 5)
                .add(Aspect.LIFE, 5)
                .add(Aspect.EARTH, 5)
        );
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.HUMAN_FLESH_COOKED), new AspectList()
                .add(Aspect.MAN, 5)
                .add(Aspect.LIFE, 5)
                .add(Aspect.CRAFT, 1)
        );
        ThaumcraftApi.registerObjectTag(new ItemStack(ModItems.AETHEREUS), new AspectList()
                .add(Aspect.SOUL, 8)
                .add(Aspect.ALCHEMY, 5)
                .add(Aspect.MAN, 2)
                .add(Aspect.WATER, 5)
        );
    }
}
