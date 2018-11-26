package ladysnake.dissolution.common.compat;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.library.InspirationsRegistry;
import knightminer.inspirations.library.recipe.cauldron.CauldronFluidRecipe;
import knightminer.inspirations.library.recipe.cauldron.ICauldronRecipe;
import knightminer.inspirations.library.recipe.cauldron.ISimpleCauldronRecipe;
import ladylib.compat.EnhancedBusSubscriber;
import ladylib.compat.StateEventReceiver;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.blocks.BlockPurificationCauldron;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.init.ModPotions;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import slimeknights.mantle.util.RecipeMatch;

import java.util.Collections;
import java.util.List;

/**
 * Inspirations compatibility: add custom fluids and recipes for every potion
 */
@EnhancedBusSubscriber(value = Ref.MOD_ID, dependencies = Inspirations.modID)
public class InspirationsCompat implements StateEventReceiver {
    public static Fluid ghastWater;
    public static Fluid eauDeMort;
    public static Fluid sanguine;

    private static final ResourceLocation EAU_DE_MORT_FLUID_TEXTURE = new ResourceLocation(Ref.MOD_ID,"blocks/eau_de_mort_fluid");
    private static final ResourceLocation SANGUINE_POTION_FLUID_TEXTURE = new ResourceLocation(Ref.MOD_ID,"blocks/sanguine_fluid");

    public void preInit(FMLPreInitializationEvent event) {
        ghastWater = new Fluid("ghast_water", new ResourceLocation(Ref.MOD_ID,"blocks/ghast_water"), new ResourceLocation(Ref.MOD_ID, "blocks/ghast_water"));
        ghastWater.setUnlocalizedName(Ref.MOD_ID + ".ghast_water");
        FluidRegistry.registerFluid(ghastWater);
        eauDeMort = new Fluid("eau_de_mort", EAU_DE_MORT_FLUID_TEXTURE, EAU_DE_MORT_FLUID_TEXTURE);
        eauDeMort.setUnlocalizedName(Ref.MOD_ID + ".eau_de_mort");
        FluidRegistry.registerFluid(eauDeMort);
        sanguine = new Fluid("sanguine_potion", SANGUINE_POTION_FLUID_TEXTURE, SANGUINE_POTION_FLUID_TEXTURE);
        sanguine.setUnlocalizedName(Ref.MOD_ID + ".sanguine_potion");
        FluidRegistry.registerFluid(sanguine);
    }

    @EnhancedBusSubscriber(value = Ref.MOD_ID, dependencies = Inspirations.modID, side = Side.CLIENT)
    public static class ClientHandler {
        @SubscribeEvent
        public void onTextureStitch(TextureStitchEvent event) {
            event.getMap().registerSprite(EAU_DE_MORT_FLUID_TEXTURE);
            event.getMap().registerSprite(SANGUINE_POTION_FLUID_TEXTURE);
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        InspirationsRegistry.addCauldronRecipe(new LiquidConversionRecipe(Items.GHAST_TEAR, ICauldronRecipe.CauldronState.WATER, ICauldronRecipe.CauldronState.fluid(ghastWater)));
        InspirationsRegistry.addCauldronRecipe(new LiquidConversionRecipe(ModItems.HUMAN_BRAIN, ICauldronRecipe.CauldronState.potion(ModPotions.OBNOXIOUS), ICauldronRecipe.CauldronState.fluid(eauDeMort)));
        InspirationsRegistry.addCauldronRecipe(new LiquidConversionRecipe(ModItems.HUMAN_HEART, ICauldronRecipe.CauldronState.potion(ModPotions.OBNOXIOUS), ICauldronRecipe.CauldronState.fluid(sanguine)));
        InspirationsRegistry.addCauldronRecipe(new CauldronFluidRecipe(RecipeMatch.of(Items.ROTTEN_FLESH), ghastWater, new ItemStack(ModItems.HUMAN_FLESH_RAW), false));
        InspirationsRegistry.addCauldronRecipe(new CauldronFluidRecipe(RecipeMatch.of(Items.GLASS_BOTTLE), eauDeMort, new ItemStack(ModItems.EAU_DE_MORT), true));
        InspirationsRegistry.addCauldronRecipe(new CauldronFluidRecipe(RecipeMatch.of(Items.GLASS_BOTTLE), sanguine, new ItemStack(ModItems.SANGUINE_POTION), true));
        if (BlockPurificationCauldron.TC_BRAIN != null) {
            InspirationsRegistry.addCauldronRecipe(new CauldronFluidRecipe(RecipeMatch.of(BlockPurificationCauldron.TC_BRAIN), ghastWater, new ItemStack(ModItems.HUMAN_BRAIN), false));
        }
    }

    public static class LiquidConversionRecipe implements ISimpleCauldronRecipe {

        private final Item ingredient;
        private final CauldronState before;
        private final CauldronState after;

        public LiquidConversionRecipe(Item ingredient, CauldronState before, CauldronState after) {
            this.ingredient = ingredient;
            this.before = before;
            this.after = after;
        }

        @Override
        public List<ItemStack> getInput() {
            return Collections.singletonList(new ItemStack(ingredient));
        }

        @Override
        public Object getInputState() {
            return before.getFluid() == null ? before.getPotion() : before.getFluid();
        }

        @Override
        public Object getState() {
            return after.getFluid();
        }

        @Override
        public boolean matches(ItemStack stack, boolean boiling, int level, CauldronState state) {
            return ((state.isWater() && before.isWater()) || before.matches(state)) && level > 0 && stack.getItem() == ingredient;
        }

        @Override
        public CauldronState getState(ItemStack stack, boolean boiling, int level, CauldronState state) {
            return after;
        }

    }
}
