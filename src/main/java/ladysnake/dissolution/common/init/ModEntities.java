package ladysnake.dissolution.common.init;

import ladysnake.dissolution.client.models.entities.ModelMinionSkeleton;
import ladysnake.dissolution.client.renders.entities.*;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.entity.boss.EntityBrimstoneFire;
import ladysnake.dissolution.common.entity.boss.EntityMawOfTheVoid;
import ladysnake.dissolution.common.entity.minion.*;
import ladysnake.dissolution.common.entity.souls.EntityFaerie;
import ladysnake.dissolution.common.entity.souls.EntityFleetingSoul;
import ladysnake.dissolution.common.entity.souls.EntitySoulSpawner;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModEntities {

    private static int id = 0;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> reg = event.getRegistry();
        registerEntity(reg, EntityMinionZombie::new, "minion_zombie", 64, true);
        registerEntity(reg, EntityMinionPigZombie::new, "minion_pig_zombie", 64, true);
        registerEntity(reg, EntityMinionSkeleton::new, "minion_skeleton", 64, true);
        registerEntity(reg, EntityMinionStray::new, "minion_stray", 64, true);
        registerEntity(reg, EntityMinionWitherSkeleton::new, "minion_wither_skeleton", 64, true);
        registerEntity(reg, EntityGenericMinion::new, "generic_minion", 64, true);
        registerEntity(reg, EntityPlayerCorpse::new, "player_corpse", 64, true);
        registerEntity(reg, EntityMawOfTheVoid::new, "maw_of_the_void", 64, true);
        registerEntity(reg, EntityBrimstoneFire::new, "brimstone_fire", 32, false);
        registerEntity(reg, EntityFleetingSoul::new, "fleeting_soul", 64, true);
        registerEntity(reg, EntityFaerie::new, "faerie", 64, true);
        reg.register(createEntry(EntitySoulSpawner::new, "soul_spawner", 64, true)
                .spawn(EnumCreatureType.AMBIENT, 50, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SWAMP))
                .spawn(EnumCreatureType.AMBIENT, 50, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
                .build());
    }

    private static void registerEntity(IForgeRegistry<EntityEntry> reg, Function<World, Entity> factory, String name, int trackingRange, boolean sendsVelocityUpdates) {
        reg.register(createEntry(factory, name, trackingRange, sendsVelocityUpdates).build());
//		EntityRegistry.registerModEntity(location, entityClass, name, id++, Dissolution.instance, trackingRange, 1, sendsVelocityUpdates);
    }

    private static EntityEntryBuilder<Entity> createEntry(Function<World, Entity> entityFactory,
                                                          String name, int trackingRange, boolean sendsVelocityUpdates) {
        return EntityEntryBuilder.create()
                .entity(entityFactory.apply(null).getClass())
                .factory(entityFactory)
                .id(new ResourceLocation(Reference.MOD_ID, name), id++)
                .name(name)
                .tracker(trackingRange, 1, sendsVelocityUpdates);
    }

    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
        RenderingRegistry.registerEntityRenderingHandler(EntityMinionZombie.class, RenderMinionZombie::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityMinionPigZombie.class, manager -> new RenderMinion<>(manager, ModelZombie::new, RenderMinion.ZOMBIE_PIGMAN_TEXTURE, RenderMinion.ZOMBIE_PIGMAN_TEXTURE));
        RenderingRegistry.registerEntityRenderingHandler(EntityMinionSkeleton.class, manager -> new RenderMinion<>(manager, ModelMinionSkeleton::new, RenderMinion.SKELETON_TEXTURE, RenderMinion.SKELETON_TEXTURE));
        RenderingRegistry.registerEntityRenderingHandler(EntityMinionStray.class, manager -> new RenderMinion<>(manager, ModelMinionSkeleton::new, RenderMinion.STRAY_TEXTURE, RenderMinion.STRAY_TEXTURE, LayerMinionStrayClothing::new));
        RenderingRegistry.registerEntityRenderingHandler(EntityMinionWitherSkeleton.class, RenderMinionWitherSkeleton::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGenericMinion.class, RenderGenericMinion::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPlayerCorpse.class, RenderPlayerCorpse::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityMawOfTheVoid.class, renderManager -> new RenderBiped<>(renderManager, new ModelBiped(), 1.0f));
        RenderingRegistry.registerEntityRenderingHandler(EntityBrimstoneFire.class, RenderBrimstoneFire::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFleetingSoul.class, RenderWillOWisp::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFaerie.class, RenderWillOWisp::new);
    }

}
