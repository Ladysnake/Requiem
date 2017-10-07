package ladysnake.dissolution.common.init;

import ladysnake.dissolution.client.models.entities.ModelMinionSkeleton;
import ladysnake.dissolution.client.models.entities.ModelMinionZombie;
import ladysnake.dissolution.client.renders.entities.*;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.entity.boss.EntityBrimstoneFire;
import ladysnake.dissolution.common.entity.boss.EntityMawOfTheVoid;
import ladysnake.dissolution.common.entity.minion.*;
import ladysnake.dissolution.common.entity.souls.EntityFleetingSoul;
import ladysnake.dissolution.common.entity.souls.EntitySoulSpawner;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModEntities {
	
    public static void register() {
    	int id = 0;

    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "minion_zombie"), EntityMinionZombie.class, "minion_zombie", id++, Dissolution.instance, 64, 1, true);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "minion_pig_zombie"), EntityMinionPigZombie.class, "minion_pig_zombie", id++, Dissolution.instance, 64, 1, true);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "minion_skeleton"), EntityMinionSkeleton.class, "minion_skeleton", id++, Dissolution.instance, 64, 1, true);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "minion_stray"), EntityMinionStray.class, "minion_stray", id++, Dissolution.instance, 64, 1, true);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "minion_wither_skeleton"), EntityMinionWitherSkeleton.class, "minion_wither_skeleton", id++, Dissolution.instance, 64, 1, true);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "player_corpse"), EntityPlayerCorpse.class, "player_corpse", id++, Dissolution.instance, 64, 1, true);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "maw_of_the_void"), EntityMawOfTheVoid.class, "maw_of_the_void", id++, Dissolution.instance, 64, 1, true);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "brimstone_fire"), EntityBrimstoneFire.class, "brimstone_fire", id++, Dissolution.instance, 32, 1, false);
    	EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "fleeting_soul"), EntityFleetingSoul.class, "fleeting_soul", id++, Dissolution.instance, 64, 1, true, 0xA8E4E4, 0x00D2D2);
		EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "soul_spawner"), EntitySoulSpawner.class, "soul_spawner", id++, Dissolution.instance, 64, 1, true);
		EntityRegistry.addSpawn(EntitySoulSpawner.class, 50, 1, 1,
				EnumCreatureType.AMBIENT, BiomeDictionary.getBiomes(BiomeDictionary.Type.SWAMP).toArray(new Biome[0]));
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
    	RenderingRegistry.registerEntityRenderingHandler(EntityMinionZombie.class, RenderMinionZombie::new);
    	RenderingRegistry.registerEntityRenderingHandler(EntityMinionPigZombie.class, manager -> new RenderMinion<>(manager, ModelMinionZombie::new, RenderMinion.ZOMBIE_PIGMAN_MINION_TEXTURE, RenderMinion.ZOMBIE_PIGMAN_TEXTURE));
    	RenderingRegistry.registerEntityRenderingHandler(EntityMinionSkeleton.class, manager -> new RenderMinion<>(manager, ModelMinionSkeleton::new, RenderMinion.SKELETON_MINION_TEXTURES, RenderMinion.SKELETON_TEXTURE));
    	RenderingRegistry.registerEntityRenderingHandler(EntityMinionStray.class, manager -> new RenderMinion<>(manager, ModelMinionSkeleton::new, RenderMinion.STRAY_MINION_TEXTURE, RenderMinion.STRAY_TEXTURE, LayerMinionStrayClothing::new));
    	RenderingRegistry.registerEntityRenderingHandler(EntityMinionWitherSkeleton.class, RenderMinionWitherSkeleton::new);
    	RenderingRegistry.registerEntityRenderingHandler(EntityPlayerCorpse.class, RenderPlayerCorpse::new);
    	RenderingRegistry.registerEntityRenderingHandler(EntityMawOfTheVoid.class, renderManager -> new RenderBiped<>(renderManager, new ModelBiped(), 1.0f));
    	RenderingRegistry.registerEntityRenderingHandler(EntityBrimstoneFire.class, RenderBrimstoneFire::new);
    	RenderingRegistry.registerEntityRenderingHandler(EntityFleetingSoul.class, RenderWillOWisp::new);
    }

}
