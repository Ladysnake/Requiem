package ladysnake.tartaros.common.init;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.blocks.BlockCrystallizer;
import ladysnake.tartaros.common.items.ItemEyeDead;
import ladysnake.tartaros.common.items.ItemScytheIron;
import ladysnake.tartaros.common.items.ItemSepulture;
import ladysnake.tartaros.common.items.ItemSoulGem;
import ladysnake.tartaros.common.items.ItemSoulInABottle;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {
	public static Item ectoplasm, ectoplasma;
	public static ItemEyeDead eye_of_the_dead;
	public static ItemScytheIron scythe_iron;
	public static ItemSoulGem soul_gem;
	public static ItemSoulInABottle soul_in_a_bottle;
	public static ItemSepulture sepulture;

    public static void init() {
    	ectoplasm = new Item();
    	ectoplasm.setUnlocalizedName(Reference.Items.ECTOPLASM.getUnlocalizedName());
        ectoplasm.setRegistryName(Reference.Items.ECTOPLASM.getRegistryName());
        ectoplasm.setCreativeTab(Tartaros.CREATIVE_TAB);
        ectoplasma = new Item();
    	ectoplasma.setUnlocalizedName(Reference.Items.ECTOPLASMA.getUnlocalizedName());
        ectoplasma.setRegistryName(Reference.Items.ECTOPLASMA.getRegistryName());
        ectoplasma.setCreativeTab(Tartaros.CREATIVE_TAB);
        eye_of_the_dead = new ItemEyeDead();
        scythe_iron = new ItemScytheIron();
        soul_gem = new ItemSoulGem();
        soul_in_a_bottle = new ItemSoulInABottle();
        sepulture = new ItemSepulture();
    }
    
    public static void register() {
    	GameRegistry.register(ectoplasm);
    	GameRegistry.register(ectoplasma);
    	GameRegistry.register(eye_of_the_dead);
    	GameRegistry.register(scythe_iron);
    	GameRegistry.register(soul_gem);
    	GameRegistry.register(soul_in_a_bottle);
    	GameRegistry.register(sepulture);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
    	registerRender(ectoplasm);
    	registerRender(ectoplasma);
    	registerRender(eye_of_the_dead);
    	registerRender(scythe_iron);
    	registerRender(soul_gem);
    	registerRender(soul_in_a_bottle);
    	registerRender(sepulture);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRender(Item item) {
    	ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + item.getUnlocalizedName().toString().substring(5)));
    }
}
