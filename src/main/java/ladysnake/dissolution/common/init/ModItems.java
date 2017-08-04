package ladysnake.dissolution.common.init;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.items.ItemBaseResource;
import ladysnake.dissolution.common.items.ItemDebug;
import ladysnake.dissolution.common.items.ItemEyeDead;
import ladysnake.dissolution.common.items.ItemGrandFaux;
import ladysnake.dissolution.common.items.ItemScarabOfEternity;
import ladysnake.dissolution.common.items.ItemScytheIron;
import ladysnake.dissolution.common.items.ItemSepulture;
import ladysnake.dissolution.common.items.ItemSoulGem;
import ladysnake.dissolution.common.items.ItemSoulInABottle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModItems {
	
	/**Used to register stuff*/
	static final ModItems INSTANCE = new ModItems();
	
	public static ItemDebug DEBUG_ITEM;
	public static ItemEyeDead EYE_OF_THE_UNDEAD;
	public static ItemGrandFaux GRAND_FAUX;
	public static ItemBaseResource BASE_RESOURCE;
	public static ItemScarabOfEternity SCARAB_OF_ETERNITY;
	public static ItemScytheIron SCYTHE_IRON;
	public static ItemSoulGem SOUL_GEM;
	public static ItemSoulInABottle SOUL_IN_A_BOTTLE;
	public static ItemSepulture SEPULTURE;
	
	static List<Item> blocks = new ArrayList<>();

//	private IForgeRegistry<Item> reg;

	void init() {
		BASE_RESOURCE = new ItemBaseResource();
		DEBUG_ITEM = new ItemDebug();
		EYE_OF_THE_UNDEAD = new ItemEyeDead();
		GRAND_FAUX = new ItemGrandFaux();
		SCARAB_OF_ETERNITY = new ItemScarabOfEternity();
		SCYTHE_IRON = new ItemScytheIron();
		SOUL_GEM = new ItemSoulGem();
		SOUL_IN_A_BOTTLE = new ItemSoulInABottle();
		SEPULTURE = new ItemSepulture();
	}
	
	private Item generateBase(Reference.Items names) {
		return new Item().setUnlocalizedName(names.getUnlocalizedName()).setRegistryName(names.getRegistryName());
	}

	@SubscribeEvent
	public void onRegister(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		reg.register(BASE_RESOURCE);
		reg.register(DEBUG_ITEM);
		reg.register(EYE_OF_THE_UNDEAD);
		reg.register(GRAND_FAUX);
		reg.register(SCARAB_OF_ETERNITY);
		reg.register(SCYTHE_IRON);
		reg.register(SOUL_GEM);
		reg.register(SOUL_IN_A_BOTTLE);
		reg.register(SEPULTURE);
		blocks.forEach(reg::register);
	}
	
	void registerOres() {
		OreDictionary.registerOre("dustSulfur", ItemBaseResource.resourceFromName("sulfur"));
		OreDictionary.registerOre("itemCinnabar", ItemBaseResource.resourceFromName("cinnabar"));
		OreDictionary.registerOre("itemMercury", ItemBaseResource.resourceFromName("mercury"));
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerRenders(ModelRegistryEvent event) {
		for (int i = 0; i < BASE_RESOURCE.variants.size(); ++i) {
			registerRender(BASE_RESOURCE, i, BASE_RESOURCE.variants.get(i));
		}
		registerRender(DEBUG_ITEM);
		registerRender(EYE_OF_THE_UNDEAD);
		registerRender(GRAND_FAUX);
		registerRender(SCARAB_OF_ETERNITY);
		registerRender(SCYTHE_IRON);
		registerRender(SOUL_GEM);
		registerRender(SOUL_IN_A_BOTTLE);
		registerRender(SEPULTURE);

	}

	@SideOnly(Side.CLIENT)
	private void registerRender(Item item) {
		registerRender(item, 0, item.getUnlocalizedName().substring(5));
	}

	@SideOnly(Side.CLIENT)
	private void registerRender(Item item, int metadata, String name) {
		ModelLoader.setCustomModelResourceLocation(item, metadata,
				new ModelResourceLocation(Reference.MOD_ID + ":" + name));
	}
	
	private ModItems() {}
}
