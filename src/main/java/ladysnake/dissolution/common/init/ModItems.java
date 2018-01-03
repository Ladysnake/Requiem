package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.items.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public final class ModItems {

    /**
     * Used to register stuff
     */
    static final ModItems INSTANCE = new ModItems();

    public static Item PESTLE;
    public static ItemLogo LOGO;
    public static ItemDebug DEBUG_ITEM;
    public static ItemJar GLASS_JAR;
    public static ItemScythe IRON_SCYTHE;
    public static ItemScythe LURKING_SCYTHE;
    public static ItemBurial SEPULTURE;
    public static ItemBurial WOODEN_COFFIN;
    public static ItemBurial OBSIDIAN_COFFIN;
    public static ItemSoulInAJar SOUL_IN_A_JAR;

    static Set<Item> allItems = new HashSet<>();

    @SuppressWarnings("unchecked")
    static <T extends Item> T name(T item, String name) {
        return (T) item.setUnlocalizedName(name).setRegistryName(new ResourceLocation(Reference.MOD_ID, name));
    }

    @SubscribeEvent
    public void onRegister(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> reg = event.getRegistry();
        // first row of additions : items that go into the creative tab
        Collections.addAll(allItems,
                GLASS_JAR = name(new ItemJar(), "glass_jar"),
                IRON_SCYTHE = name((ItemScythe) new ItemScythe(ToolMaterial.IRON).setMaxDamage(255), "iron_scythe"),
                LURKING_SCYTHE = name((ItemScythe) new ItemScythe(ToolMaterial.DIAMOND).setMaxDamage(510), "lurking_scythe"));

        allItems.forEach(i -> i.setCreativeTab(Dissolution.CREATIVE_TAB));

        // second row of additions : hidden items
        Collections.addAll(allItems,
                DEBUG_ITEM = name(new ItemDebug(), "debug_item"),
                LOGO = name(new ItemLogo(), "logo")
        );

        allItems.forEach(reg::register);
    }

    @SubscribeEvent
    public void remapIds(RegistryEvent.MissingMappings<Item> event) {
        List<Mapping<Item>> missingBlocks = event.getMappings();
        Map<String, Item> remaps = new HashMap<>();
        remaps.put("itemdebug", DEBUG_ITEM);
        remaps.put("itemgrandfaux", IRON_SCYTHE);
        remaps.put("itemsepulture", SEPULTURE);
        remaps.put("itemsoulinabottle", SOUL_IN_A_JAR);
        remaps.put("itemironscythe", IRON_SCYTHE);
        for (Mapping<Item> map : missingBlocks) {
            if (map.key.getResourceDomain().equals(Reference.MOD_ID)) {
                if (ModBlocks.INSTANCE.remaps.get(map.key.getResourcePath()) != null)
                    map.remap(Item.getItemFromBlock(ModBlocks.INSTANCE.remaps.get(map.key.getResourcePath())));
                if (remaps.get(map.key.getResourcePath()) != null)
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
        if (item instanceof ICustomLocation)
            ((ICustomLocation) item).registerRender();
        else
            registerRender(item, new ModelResourceLocation(item.getRegistryName().toString()));
    }

    @SideOnly(Side.CLIENT)
    private void registerRender(Item item, ModelResourceLocation loc) {
        ModelLoader.setCustomModelResourceLocation(item, 0, loc);
    }

    private ModItems() {
    }
}
