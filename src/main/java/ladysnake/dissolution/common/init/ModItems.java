package ladysnake.tartaros.common.init;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.items.ItemBaseResource;
import ladysnake.tartaros.common.items.ItemDebug;
import ladysnake.tartaros.common.items.ItemEyeDead;
import ladysnake.tartaros.common.items.ItemGrandFaux;
import ladysnake.tartaros.common.items.ItemScytheIron;
import ladysnake.tartaros.common.items.ItemSepulture;
import ladysnake.tartaros.common.items.ItemSoulGem;
import ladysnake.tartaros.common.items.ItemSoulInABottle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {
	public static Item ECTOPLASM, ECTOPLASMA;
	public static ItemDebug DEBUG_ITEM;
	public static ItemEyeDead EYE_OF_THE_UNDEAD;
	public static ItemGrandFaux GRAND_FAUX;
	public static ItemBaseResource BASE_RESOURCE;
	public static ItemScytheIron SCYTHE_IRON;
	public static ItemSoulGem SOUL_GEM;
	public static ItemSoulInABottle SOUL_IN_A_BOTTLE;
	public static ItemSepulture SEPULTURE;

    public static void init() {
    	BASE_RESOURCE = new ItemBaseResource();
    	DEBUG_ITEM = new ItemDebug();
    	ECTOPLASM = new Item();
    	ECTOPLASM.setUnlocalizedName(Reference.Items.ECTOPLASM.getUnlocalizedName());
        ECTOPLASM.setRegistryName(Reference.Items.ECTOPLASM.getRegistryName());
        ECTOPLASM.setCreativeTab(Tartaros.CREATIVE_TAB);
        ECTOPLASMA = new Item();
    	ECTOPLASMA.setUnlocalizedName(Reference.Items.ECTOPLASMA.getUnlocalizedName());
        ECTOPLASMA.setRegistryName(Reference.Items.ECTOPLASMA.getRegistryName());
        ECTOPLASMA.setCreativeTab(Tartaros.CREATIVE_TAB);
        EYE_OF_THE_UNDEAD = new ItemEyeDead();
        SCYTHE_IRON = new ItemScytheIron();
        GRAND_FAUX = new ItemGrandFaux();
        SOUL_GEM = new ItemSoulGem();
        SOUL_IN_A_BOTTLE = new ItemSoulInABottle();
        SEPULTURE = new ItemSepulture();
    }
    
    public static void register() {
    	GameRegistry.register(BASE_RESOURCE);
    	GameRegistry.register(DEBUG_ITEM);
    	GameRegistry.register(ECTOPLASM);
    	GameRegistry.register(ECTOPLASMA);
    	GameRegistry.register(EYE_OF_THE_UNDEAD);
    	GameRegistry.register(GRAND_FAUX);
    	GameRegistry.register(SCYTHE_IRON);
    	GameRegistry.register(SOUL_GEM);
    	GameRegistry.register(SOUL_IN_A_BOTTLE);
    	GameRegistry.register(SEPULTURE);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
    	registerRender(BASE_RESOURCE);
    	registerRender(DEBUG_ITEM);
    	registerRender(ECTOPLASM);
    	registerRender(ECTOPLASMA);
    	registerRender(EYE_OF_THE_UNDEAD);
    	registerRender(GRAND_FAUX);
    	registerRender(SCYTHE_IRON);
    	registerRender(SOUL_GEM);
    	registerRender(SOUL_IN_A_BOTTLE);
    	registerRender(SEPULTURE);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRender(Item item) {
    	ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + item.getUnlocalizedName().toString().substring(5)));
    }
}
