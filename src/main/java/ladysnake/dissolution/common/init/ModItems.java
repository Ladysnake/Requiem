package ladysnake.dissolution.common.init;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.items.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

@SuppressWarnings("WeakerAccess")
public final class ModItems {
	
	/**Used to register stuff*/
	static final ModItems INSTANCE = new ModItems();

	public static Item CINNABAR;
	public static Item HALITE;
	public static Item SULFUR;
	public static ItemAcerbacaFruit ACERBACA;
	public static ItemFood INSUBACA;
	public static ItemFood LIMOBACA;
	public static ItemAcerbacaFruit SALERBACA;
	public static ItemCasing WOODEN_CASING;
	public static ItemDebug DEBUG_ITEM;
	public static ItemDepleted DEPLETED_CLAY;
	public static ItemDepletedCoal DEPLETED_COAL;
	public static ItemEyeUndead EYE_OF_THE_UNDEAD;
	public static ItemEyeOfDiscord EYE_OF_DISCORD;
	public static ItemOccularePart BRAIN;
	public static ItemOccularePart DIAMOND_SHELL;
	public static ItemOccularePart EMERALD_SHELL;
	public static ItemOccularePart IRON_SHELL;
	public static ItemOccularePart GOLD_SHELL;
	public static ItemOccularePart TIRED_ETCHING;
	public static ItemPlug PLUG;
	public static ItemScythe IRON_SCYTHE;
	public static ItemScythe LURKING_SCYTHE;
	public static ItemSeeds BACA_SEEDS;
	public static ItemSepulture SEPULTURE;
	public static ItemSoulInAFlask SOUL_IN_A_FLASK;

	static Set<Item> allItems = new HashSet<>();
	
	@SuppressWarnings("unchecked")
    private static <T extends Item> T name(T item, String name) {
		return (T) item.setUnlocalizedName(name).setRegistryName(name);
	}

	@SubscribeEvent
	public void onRegister(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		//noinspection ConstantConditions
		Collections.addAll(allItems,
				CINNABAR = name(new Item(), "cinnabar"),
				HALITE = name(new Item(), "halite"),
				SULFUR = name(new Item(), "sulfur"),
				ACERBACA = name(new ItemAcerbacaFruit(0, 3f, IIncorporealHandler.CorporealityStatus.SOUL), "acerbaca"),
				INSUBACA = name(new ItemFood(-3, -0.3f, false).setPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:poison"), 20), 0.95f), "insubaca"),
				LIMOBACA = name(new ItemFood(5, 1.2f, false), "limobaca"),
				SALERBACA = name(new ItemAcerbacaFruit(1, 2.5f, IIncorporealHandler.CorporealityStatus.ECTOPLASM), "salerbaca"),
				WOODEN_CASING = name(new ItemCasing(), "wooden_machine_casing"),
				DEBUG_ITEM = name(new ItemDebug(), "debug_item"),
				DEPLETED_CLAY = name(new ItemDepleted(), "depleted_clay_ball"),
				DEPLETED_COAL = name(new ItemDepletedCoal(), "depleted_coal"),
				EYE_OF_THE_UNDEAD = name(new ItemEyeUndead(), "eye_of_the_undead"),
				EYE_OF_DISCORD = name(new ItemEyeOfDiscord(), "eye_of_discord"),
				BRAIN = name(new ItemOccularePart(), "occulare_brain"),
				DIAMOND_SHELL = name(new ItemOccularePart(1500), "diamond_occulare_shell"),
				EMERALD_SHELL = name(new ItemOccularePart(750), "emerald_occulare_shell"),
				GOLD_SHELL = name(new ItemOccularePart(50), "gold_occulare_shell"),
				IRON_SHELL = name(new ItemOccularePart(500), "iron_occulare_shell"),
				TIRED_ETCHING = name(new ItemOccularePart(), "tired_etching"),
				PLUG = name(new ItemPlug(), "plug"),
				IRON_SCYTHE = name((ItemScythe) new ItemScythe(ToolMaterial.IRON).setMaxDamage(255), "iron_scythe"),
				LURKING_SCYTHE = name((ItemScythe) new ItemScythe(ToolMaterial.DIAMOND).setMaxDamage(510), "lurking_scythe"),
				BACA_SEEDS = name(new ItemSeeds(Blocks.LEAVES, Blocks.GRASS), "baca_seeds"),
				SEPULTURE = name(new ItemSepulture(), "sepulture"),
				SOUL_IN_A_FLASK = name(new ItemSoulInAFlask(), "will_o_wisp_flask"));

		AlchemyModuleTypes.registerItems(allItems);

		for(Item i : allItems) {
			reg.register(i);
			if(i != DEBUG_ITEM)
				i.setCreativeTab(Dissolution.CREATIVE_TAB);
		}
	}
	
	void registerOres() {
		OreDictionary.registerOre("blockClay", ModBlocks.DEPLETED_CLAY);
		OreDictionary.registerOre("clay", DEPLETED_CLAY);
		OreDictionary.registerOre("blockCoal", ModBlocks.DEPLETED_COAL);
		OreDictionary.registerOre("coal", DEPLETED_COAL);
		OreDictionary.registerOre("blockMagma", ModBlocks.DEPLETED_MAGMA);
	}
	
	@SubscribeEvent
    public void remapIds(RegistryEvent.MissingMappings<Item> event) {
    	List<Mapping<Item>> missingBlocks = event.getMappings();
    	Map<String, Item> remaps = new HashMap<>();
    	remaps.put("itemdebug", DEBUG_ITEM);
    	remaps.put("itemeyeofundead", EYE_OF_THE_UNDEAD);
    	remaps.put("itemgrandfaux", IRON_SCYTHE);
    	remaps.put("itemsepulture", SEPULTURE);
    	remaps.put("itemsoulinabottle", SOUL_IN_A_FLASK);
    	remaps.put("itemironscythe", IRON_SCYTHE);
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
		allItems.stream()
				.filter(itemIn -> !(Block.getBlockFromItem(itemIn) instanceof BlockFluidBase))
				.forEach(this::registerRender);
	}

	@SideOnly(Side.CLIENT)
	private void registerRender(Item item) {
		assert item.getRegistryName() != null;
		registerRender(item, item instanceof ICustomLocation
				? ((ICustomLocation)item).getModelLocation()
				: new ModelResourceLocation(item.getRegistryName().toString()));
	}
	
	@SideOnly(Side.CLIENT)
	private void registerRender(Item item, ModelResourceLocation loc) {
		ModelLoader.setCustomModelResourceLocation(item, 0, loc);
	}
	
	private ModItems() {}
}
