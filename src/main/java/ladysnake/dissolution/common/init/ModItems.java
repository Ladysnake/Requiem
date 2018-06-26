package ladysnake.dissolution.common.init;

import ladylib.registration.AutoRegister;
import ladysnake.dissolution.common.OreDictHelper;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.items.ItemAethereus;
import ladysnake.dissolution.common.items.ItemDebug;
import ladysnake.dissolution.common.items.ItemHumanFlesh;
import ladysnake.dissolution.common.items.ItemLogo;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@SuppressWarnings({"WeakerAccess", "unused"})
@AutoRegister(Reference.MOD_ID)
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public final class ModItems {

    /**
     * Used to register stuff
     */
    static final ModItems INSTANCE = new ModItems();

    @AutoRegister.Unlisted
    public static final ItemLogo LOGO = new ItemLogo();
    @AutoRegister.Unlisted
    @AutoRegister.OldNames("itemdebug")
    public static final ItemDebug DEBUG_ITEM = new ItemDebug();
    public static final Item AETHEREUS = new ItemAethereus().setMaxStackSize(1);
    @AutoRegister.Ore(OreDictHelper.HUMAN_FLESH_RAW)
    public static final ItemFood HUMAN_FLESH_RAW = new ItemHumanFlesh(4, 4, true).setAlwaysEdible().setPotionEffect(new PotionEffect(MobEffects.NAUSEA, 15*20), 0.6f);
    @AutoRegister.Ore(OreDictHelper.HUMAN_FLESH_COOKED)
    public static final ItemFood HUMAN_FLESH_COOKED = new ItemFood(8, false).setAlwaysEdible();

    @SubscribeEvent
    public void onRegistryRegister(RegistryEvent.Register<IRecipe> event) {
        GameRegistry.addSmelting(HUMAN_FLESH_RAW, new ItemStack(HUMAN_FLESH_COOKED), 0.35f);
    }

    private ModItems() {
    }
}
