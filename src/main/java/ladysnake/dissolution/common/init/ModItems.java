package ladysnake.dissolution.common.init;

import ladylib.registration.AutoRegister;
import ladysnake.dissolution.common.OreDictHelper;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.items.ItemDebug;
import ladysnake.dissolution.common.items.ItemJar;
import ladysnake.dissolution.common.items.ItemLogo;
import ladysnake.dissolution.common.items.ItemScythe;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemFood;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;

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
    @AutoRegister.Ignore
    public static final ItemJar GLASS_JAR = new ItemJar();
    @AutoRegister.OldNames({"itemironscythe", "itemgrandfaux"})
    public static final ItemScythe IRON_SCYTHE = new ItemScythe(ToolMaterial.IRON).setMaxDamage(255);
    public static final ItemScythe LURKING_SCYTHE = new ItemScythe(ToolMaterial.DIAMOND).setMaxDamage(510);
    @AutoRegister.Ore(OreDictHelper.HUMAN_FLESH_RAW)
    public static final ItemFood HUMAN_FLESH_RAW = new ItemFood(4, true);
    @AutoRegister.Ore(OreDictHelper.HUMAN_FLESH_COOKED)
    public static final ItemFood HUMAN_FLESH_COOKED = new ItemFood(8, false);

    @NotNull
    @SuppressWarnings("ConstantConditions")
    public static <T> T nonNullInjected() {
        return null;
    }

    //    @SubscribeEvent
//    public void remapIds(RegistryEvent.MissingMappings<Item> event) {
//        List<Mapping<Item>> missingBlocks = event.getMappings();
//        Map<String, Item> remaps = new HashMap<>();
//        remaps.put("itemdebug", DEBUG_ITEM);
//        remaps.put("itemgrandfaux", IRON_SCYTHE);
//        remaps.put("itemsepulture", SEPULTURE);
//        remaps.put("itemsoulinabottle", WISP_IN_A_JAR);
//        remaps.put("itemironscythe", IRON_SCYTHE);
//        for (Mapping<Item> map : missingBlocks) {
//            if (map.key.getResourceDomain().equals(Reference.MOD_ID)) {
//                if (ModBlocks.INSTANCE.remaps.get(map.key.getResourcePath()) != null) {
//                    map.remap(Item.getItemFromBlock(ModBlocks.INSTANCE.remaps.get(map.key.getResourcePath())));
//                }
//                if (remaps.get(map.key.getResourcePath()) != null) {
//                    map.remap(remaps.get(map.key.getResourcePath()));
//                }
//            }
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @SubscribeEvent
//    public void registerRenders(ModelRegistryEvent event) {
//        allItems.stream()
//                .filter(itemIn -> !(Block.getBlockFromItem(itemIn) instanceof BlockFluidBase))
//                .forEach(this::registerRender);
//    }
//
//    @SideOnly(Side.CLIENT)
//    private void registerRender(Item item) {
//        assert item.getRegistryName() != null;
//        if (item instanceof ICustomLocation) {
//            ((ICustomLocation) item).registerRender();
//        } else {
//            registerRender(item, new ModelResourceLocation(item.getRegistryName().toString()));
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    private void registerRender(Item item, ModelResourceLocation loc) {
//        ModelLoader.setCustomModelResourceLocation(item, 0, loc);
//    }

    private ModItems() {
    }
}
