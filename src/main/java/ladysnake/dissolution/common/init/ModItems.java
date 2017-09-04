package ladysnake.dissolution.common.init;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.items.ItemCasing;
import ladysnake.dissolution.common.items.ItemDebug;
import ladysnake.dissolution.common.items.ItemEyeDead;
import ladysnake.dissolution.common.items.ItemScythe;
import ladysnake.dissolution.common.items.ItemSepulture;
import ladysnake.dissolution.common.items.ItemSoulGem;
import ladysnake.dissolution.common.items.ItemSoulInABottle;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModItems {
	
	/**Used to register stuff*/
	static final ModItems INSTANCE = new ModItems();

	public static ItemScythe ANCIENT_SCYTHE;
	public static Item CINNABAR;
	public static ItemDebug DEBUG_ITEM;
	public static ItemEyeDead EYE_OF_THE_UNDEAD;
	public static Item HALITE;
	public static Item MODULAR_CONTAINER;
	public static Item MODULAR_INTERFACE;
	public static Item MODULAR_PIPE;
	public static ItemScythe SCYTHE_IRON;
	public static ItemSepulture SEPULTURE;
	public static ItemSoulGem SOUL_GEM;
	public static ItemSoulInABottle SOUL_IN_A_BOTTLE;
	public static Item SULFUR;
	public static ItemCasing WOODEN_CASING;
	
	static Set<Item> allItems = new HashSet<>();
	
	private static <T extends Item> T name(T item, Reference.Items names) {
		return (T) item.setUnlocalizedName(names.getUnlocalizedName()).setRegistryName(names.getRegistryName());
	}
	
	private static <T extends Item> T name(T item, String name) {
		return (T) item.setUnlocalizedName(name).setRegistryName(name);
	}

	@SubscribeEvent
	public void onRegister(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		Collections.addAll(allItems, 
				DEBUG_ITEM = name(new ItemDebug(), Reference.Items.DEBUG), 
				CINNABAR = name(new Item(), Reference.Items.CINNABAR),
				WOODEN_CASING = name(new ItemCasing(), "wooden_casing"),
				EYE_OF_THE_UNDEAD = name(new ItemEyeDead(), Reference.Items.EYE_DEAD), 
				ANCIENT_SCYTHE = name((ItemScythe) new ItemScythe(ToolMaterial.DIAMOND).setMaxDamage(1500), Reference.Items.ANCIENT_SCYTHE), 
				SCYTHE_IRON = name((ItemScythe) new ItemScythe(ToolMaterial.IRON).setMaxDamage(255), Reference.Items.SCYTHE_IRON),
				MODULAR_CONTAINER = name(new Item(), "modular_container"),
				MODULAR_INTERFACE = name(new Item(), "modular_interface"),
				MODULAR_PIPE = name(new Item(), "modular_pipe"),
				SOUL_GEM = name(new ItemSoulGem(), Reference.Items.SOULGEM), 
				SOUL_IN_A_BOTTLE = name(new ItemSoulInABottle(), Reference.Items.SOULINABOTTLE), 
				SULFUR = name(new Item(), Reference.Items.SULFUR),
				SEPULTURE = name(new ItemSepulture(), Reference.Items.SEPULTURE));
		for(Item i : allItems) {
			reg.register(i);
			if(i != DEBUG_ITEM)
				i.setCreativeTab(Dissolution.CREATIVE_TAB);
		}
	}
	
	void registerOres() {}
	
	@SubscribeEvent
    public void remapIds(RegistryEvent.MissingMappings<Item> event) {
    	List<Mapping<Item>> missingBlocks = event.getMappings();
    	Map<String, Item> remaps = new HashMap<>();
    	remaps.put("itemdebug", DEBUG_ITEM);
    	remaps.put("itemeyeofundead", EYE_OF_THE_UNDEAD);
    	remaps.put("itemgrandfaux", SCYTHE_IRON);
    	remaps.put("itemsepulture", SEPULTURE);
    	remaps.put("itemsoulgem", SOUL_GEM);
    	remaps.put("itemsoulinabottle", SOUL_IN_A_BOTTLE);
    	remaps.put("itemironscythe", SCYTHE_IRON);
    	for(Mapping<Item> map : missingBlocks) {
    		if(map.key.getResourceDomain().equals(Reference.MOD_ID)) {
    			if(ModBlocks.INSTANCE.remaps.get(map.key.getResourcePath()) != null)
    				map.remap(Item.getItemFromBlock(ModBlocks.INSTANCE.remaps.get(map.key.getResourcePath())));
    			if(remaps.get(map.key.getResourcePath()) != null)
    				map.remap(remaps.get(map.key.getResourcePath()));
    		}
    	}
    }

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerRenders(ModelRegistryEvent event) {
		allItems.stream().filter(itemIn -> !(Block.getBlockFromItem(itemIn) instanceof BlockFluidBase)).forEach(this::registerRender);
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
