package ladysnake.dissolution.common.init;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ladysnake.dissolution.client.renders.entities.RenderMinionSkeleton;
import ladysnake.dissolution.client.renders.entities.RenderMinionStray;
import ladysnake.dissolution.client.renders.entities.RenderMinionZombie;
import ladysnake.dissolution.client.renders.entities.RenderWanderingSoul;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.Tartaros;
import ladysnake.dissolution.common.entity.EntityMinionSkeleton;
import ladysnake.dissolution.common.entity.EntityMinionStray;
import ladysnake.dissolution.common.entity.EntityMinionZombie;
import ladysnake.dissolution.common.entity.EntityWanderingSoul;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModEntities {
	
	private static final Map<String, Field> iamabitch = new HashMap<>();
	
    public static void register() {
    	/*
    	for (Field f : FMLMessage.EntitySpawnMessage.class.getDeclaredFields()) {
			f.setAccessible(true);
//			System.out.println(f.getName());
			iamabitch.put(f.getName(), f);
		}
    	for (Field f : FMLMessage.EntityMessage.class.getDeclaredFields()) {
			f.setAccessible(true);
//			System.out.println(f.getName());
			iamabitch.put(f.getName(), f);
		}
    	*/
    	int id = 0;
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID + ":wandering_soul"), EntityWanderingSoul.class, "wandering_soul", id++, Tartaros.instance, 64, 1, true, 0xA8E4E4, 0x00D2D2);
    	EntityRegistry.addSpawn(EntityWanderingSoul.class, 50, 1, 1, EnumCreatureType.CREATURE, Biomes.HELL);
    	LootTableList.register(EntityWanderingSoul.LOOT);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID + ":minion_zombie"), EntityMinionZombie.class, "minion_zombie", id++, Tartaros.instance, 64, 1, true);
    	/*
    	EntityRegistration er = EntityRegistry.instance().lookupModSpawn(Loader.instance().getIndexedModList().get(Reference.MOD_ID), id-1);
    	er.setCustomSpawning((FMLMessage.EntitySpawnMessage spawnMsg) -> 
    		{
				try {
			        WorldClient wc = FMLClientHandler.instance().getWorldClient();
					EntityMinionZombie src = (EntityMinionZombie) wc.getEntityByID((int) iamabitch.get("entity").get(spawnMsg));//(EntityMinionZombie)(iamabitch.get("entity").get(spawnMsg));
					System.out.println(src);
					EntityMinionZombie ret = new EntityMinionZombie(src.getEntityWorld(), src.isHusk());
	                ret.setEntityId(src.getEntityId());
	                ret.setUniqueId(src.getUniqueID());
	                ret.setLocationAndAngles((double)(iamabitch.get("rawX").get(spawnMsg)), (double)(iamabitch.get("rawY").get(spawnMsg)), 
	                		(double)(iamabitch.get("rawZ").get(spawnMsg)), (float)(iamabitch.get("scaledYaw").get(spawnMsg)), 
	                		(float)(iamabitch.get("scaledPitch").get(spawnMsg)));
                    ret.rotationYawHead = (float)(iamabitch.get("scaledHeadYaw").get(spawnMsg));

					return ret;		//TODO finish this function
				} catch (IllegalArgumentException | IllegalAccessException | NullPointerException e) {
					e.printStackTrace();
					return null;
				}
			}, false);
    	*/
//    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID + ":minion_husk"), EntityMinionHusk.class, "minion_husk", id++, Tartaros.instance, 64, 1, true);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID + ":minion_skeleton"), EntityMinionSkeleton.class, "minion_skeleton", id++, Tartaros.instance, 64, 1, true);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID + ":minion_stray"), EntityMinionStray.class, "minion_stray", id++, Tartaros.instance, 64, 1, true);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
    	RenderingRegistry.registerEntityRenderingHandler(EntityWanderingSoul.class, new RenderWanderingSoul.Factory());
    	RenderingRegistry.registerEntityRenderingHandler(EntityMinionZombie.class, new RenderMinionZombie.Factory());
    	RenderingRegistry.registerEntityRenderingHandler(EntityMinionSkeleton.class, new RenderMinionSkeleton.Factory());
    	RenderingRegistry.registerEntityRenderingHandler(EntityMinionStray.class, new RenderMinionStray.Factory());
    }

}
