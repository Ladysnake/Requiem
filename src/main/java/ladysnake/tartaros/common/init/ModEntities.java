package ladysnake.tartaros.common.init;

import ladysnake.tartaros.client.renders.RenderWanderingSoul;
import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.blocks.BlockCrystallizer;
import ladysnake.tartaros.common.entity.EntityWanderingSoul;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModEntities {
    
    public static void register() {
    	int id = 0;
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID + ":wandering_soul"), EntityWanderingSoul.class, "wandering_soul", id++, Tartaros.instance, 64, 1, true, 0xA8E4E4, 0x00D2D2);
    	EntityRegistry.addSpawn(EntityWanderingSoul.class, 100, 1, 1, EnumCreatureType.CREATURE, Biomes.HELL);
    	LootTableList.register(EntityWanderingSoul.LOOT);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
    	RenderingRegistry.registerEntityRenderingHandler(EntityWanderingSoul.class, new RenderWanderingSoul.Factory());
    }

}
