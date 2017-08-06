package ladysnake.dissolution.common.init;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.items.ItemBaseResource;
import ladysnake.dissolution.common.items.ItemDebug;
import ladysnake.dissolution.common.items.ItemEyeDead;
import ladysnake.dissolution.common.items.ItemScythe;
import ladysnake.dissolution.common.items.ItemSepulture;
import ladysnake.dissolution.common.items.ItemSoulGem;
import ladysnake.dissolution.common.items.ItemSoulInABottle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
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
	public static ItemScythe GRAND_FAUX;
	public static ItemBaseResource BASE_RESOURCE;
	public static Item SCARAB_OF_ETERNITY;
	public static ItemScythe SCYTHE_IRON;
	public static ItemSoulGem SOUL_GEM;
	public static ItemSoulInABottle SOUL_IN_A_BOTTLE;
	public static ItemSepulture SEPULTURE;
	
	static Set<Item> allItems = new HashSet<>();
	
	private <T extends Item> T giveNames(T item, Reference.Items names) {
		return (T) item.setUnlocalizedName(names.getUnlocalizedName()).setRegistryName(names.getRegistryName());
	}

	@SubscribeEvent
	public void onRegister(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		Collections.addAll(allItems, 
				BASE_RESOURCE = giveNames(new ItemBaseResource(), Reference.Items.BASE_RESOURCE), 
				DEBUG_ITEM = giveNames(new ItemDebug(), Reference.Items.DEBUG), 
				EYE_OF_THE_UNDEAD = giveNames(new ItemEyeDead(), Reference.Items.EYE_DEAD), 
				GRAND_FAUX = giveNames((ItemScythe) new ItemScythe(ToolMaterial.DIAMOND).setMaxDamage(1500), Reference.Items.GRAND_FAUX), 
				SCARAB_OF_ETERNITY = giveNames(new Item().setMaxStackSize(1), Reference.Items.SCARAB_OF_ETERNITY), 
				SCYTHE_IRON = giveNames((ItemScythe) new ItemScythe(ToolMaterial.IRON).setMaxDamage(255), Reference.Items.SCYTHE_IRON),
				SOUL_GEM = giveNames(new ItemSoulGem(), Reference.Items.SOULGEM), 
				SOUL_IN_A_BOTTLE = giveNames(new ItemSoulInABottle(), Reference.Items.SOULINABOTTLE), 
				SEPULTURE = giveNames(new ItemSepulture(), Reference.Items.SEPULTURE));
		for(Item i : allItems) {
			reg.register(i);
			if(i != DEBUG_ITEM)
				i.setCreativeTab(Dissolution.CREATIVE_TAB);
		}
	}
	
	void registerOres() {
		OreDictionary.registerOre("dustSulfur", ItemBaseResource.resourceFromName("sulfur"));
		OreDictionary.registerOre("itemCinnabar", ItemBaseResource.resourceFromName("cinnabar"));
		OreDictionary.registerOre("itemMercury", ItemBaseResource.resourceFromName("mercury"));
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerRenders(ModelRegistryEvent event) {
		allItems.forEach(this::registerRender);
		for (int i = 0; i < BASE_RESOURCE.variants.size(); ++i) {
			registerRender(BASE_RESOURCE, i, BASE_RESOURCE.variants.get(i));
		}
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
