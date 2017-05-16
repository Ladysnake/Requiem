package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.items.ItemBaseResource;
import ladysnake.dissolution.common.items.ItemDebug;
import ladysnake.dissolution.common.items.ItemEyeDead;
import ladysnake.dissolution.common.items.ItemGrandFaux;
import ladysnake.dissolution.common.items.ItemScytheIron;
import ladysnake.dissolution.common.items.ItemSepulture;
import ladysnake.dissolution.common.items.ItemSoulGem;
import ladysnake.dissolution.common.items.ItemSoulInABottle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ModItems {
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
		GameRegistry.register(EYE_OF_THE_UNDEAD);
		GameRegistry.register(GRAND_FAUX);
		GameRegistry.register(SCYTHE_IRON);
		GameRegistry.register(SOUL_GEM);
		GameRegistry.register(SOUL_IN_A_BOTTLE);
		GameRegistry.register(SEPULTURE);
	}

	public static void registerOres() {
		OreDictionary.registerOre("dustSulfur", new ItemStack(BASE_RESOURCE, 1, 2));
		OreDictionary.registerOre("itemCinnabar", new ItemStack(BASE_RESOURCE, 1, 3));
		OreDictionary.registerOre("itemMercury", new ItemStack(BASE_RESOURCE, 1, 4));
	}

	@SideOnly(Side.CLIENT)
	public static void registerRenders() {
		for (int i = 0; i < BASE_RESOURCE.variants.size(); ++i) {
			registerRender(BASE_RESOURCE, i, BASE_RESOURCE.variants.get(i));
		}
		registerRender(DEBUG_ITEM);
		registerRender(EYE_OF_THE_UNDEAD);
		registerRender(GRAND_FAUX);
		registerRender(SCYTHE_IRON);
		registerRender(SOUL_GEM);
		registerRender(SOUL_IN_A_BOTTLE);
		registerRender(SEPULTURE);
	}

	@SideOnly(Side.CLIENT)
	public static void registerRender(Item item) {
		registerRender(item, 0, item.getUnlocalizedName().toString().substring(5));
		// ModelLoader.setCustomModelResourceLocation(item, 0, new
		// ModelResourceLocation(Reference.MOD_ID + ":" +
		// item.getUnlocalizedName().toString().substring(5)));
	}

	@SideOnly(Side.CLIENT)
	public static void registerRender(Item item, int metadata, String name) {
		ModelLoader.setCustomModelResourceLocation(item, metadata,
				new ModelResourceLocation(Reference.MOD_ID + ":" + name));
	}
}
