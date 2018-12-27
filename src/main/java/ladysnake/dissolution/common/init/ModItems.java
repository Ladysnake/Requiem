package ladysnake.dissolution.common.init;

import ladylib.compat.EnhancedBusSubscriber;
import ladylib.registration.AutoRegister;
import ladysnake.dissolution.common.OreDictHelper;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.items.*;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreIngredient;

@SuppressWarnings({"unused"})
@AutoRegister(value = Ref.MOD_ID, injectObjectHolder = true)
public final class ModItems {

    /**
     * Used to register stuff
     */
    @AutoRegister.Ignore
    @EnhancedBusSubscriber(Ref.MOD_ID)
    public static final ModItems INSTANCE = new ModItems();

    @AutoRegister.Unlisted
    public static final ItemLogo LOGO = new ItemLogo();
    @AutoRegister.Unlisted
    @AutoRegister.OldNames("itemdebug")
    public static final ItemDebug DEBUG_ITEM = new ItemDebug();
    public static final Item EAU_DE_MORT = new ItemAethereus().setMaxStackSize(1);
    public static final Item SANGUINE_POTION = new ItemSanguinePotion().setMaxStackSize(1);
    @AutoRegister.Ore(OreDictHelper.HUMAN_FLESH_RAW)
    public static final ItemFood HUMAN_FLESH_RAW = new ItemHumanFlesh(4, 4, true).setPotionEffect(new PotionEffect(MobEffects.NAUSEA, 15*20), 0.6f);
    public static final ItemFood HUMAN_BRAIN = new ItemHumanFlesh(2, 3, false);
    public static final ItemFood HUMAN_HEART = new ItemHumanFlesh(2, 3, false);

    @SubscribeEvent
    public void addRecipes(RegistryEvent.Register<IRecipe> event) {
        GameRegistry.addSmelting(HUMAN_FLESH_RAW, new ItemStack(HUMAN_BRAIN), 0.35f);
        PotionHelper.addMix(PotionTypes.WATER, new OreIngredient(OreDictHelper.HUMAN_FLESH_RAW), ModPotions.OBNOXIOUS);
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(
                PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), ModPotions.OBNOXIOUS),
                new ItemStack(ModItems.HUMAN_BRAIN),
                new ItemStack(ModItems.EAU_DE_MORT)
        ));
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(
                PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), ModPotions.OBNOXIOUS),
                new ItemStack(ModItems.HUMAN_HEART),
                new ItemStack(ModItems.SANGUINE_POTION)
        ));
    }

    private ModItems() { }
}
