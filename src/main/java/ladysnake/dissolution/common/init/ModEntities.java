package ladysnake.dissolution.common.init;

import ladylib.LadyLib;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.api.possession.DissolutionPossessionApi;
import ladysnake.dissolution.client.renders.entities.RenderPlayerCorpse;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.EntityPlayerShell;
import ladysnake.dissolution.common.entity.EntityPossessableImpl;
import ladysnake.dissolution.common.entity.PossessableEntityFactory;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModEntities {

    private static int id = 0;
    private static Field entityentry$factory;
    private static Class<?> contructorFactoryClass;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerPossessables(RegistryEvent.Register<EntityEntry> event) {
        for (EntityEntry entityEntry : event.getRegistry()) {
            Class<? extends Entity> entityClass = entityEntry.getEntityClass();
            // Nothing to do if the entity is already registered
            if (DissolutionPossessionApi.isEntityRegistered(entityClass)) {
                continue;
            }
            // Only declare new implementations for mobs that can not already be possessed
            if (EntityMob.class.isAssignableFrom(entityClass) && !IPossessable.class.isAssignableFrom(entityClass)) {
                boolean defineImpl = false;
                try {
                    // create an instance to check if it fulfills the criteria
                    Entity instance = newInstance(entityEntry);
                    defineImpl = instance != null && ((EntityMob)instance).isEntityUndead() && instance.isNonBoss();
                    // the easy way for mods to choose
                    try {
                        Method m = ReflectionHelper.findMethod(entityClass, "dissolutionGeneratePossessedVersion", null);
                        if (Modifier.isStatic(m.getModifiers()) && m.getReturnType() == boolean.class) {
                            defineImpl = (boolean) m.invoke(null);
                        }
                    } catch (ReflectionHelper.UnableToFindMethodException ignored) {
                        // ignored
                    } catch (Exception e) {
                        Dissolution.LOGGER.error("A check method has thrown an exception", e);
                    }
                } catch (Exception e) {
                    // countless mobs will probably fail due to the null world, no need to spam the log with the stacktrace
                    Dissolution.LOGGER.warn("Could not check whether to create a possessable version of {} ({})", entityEntry.getRegistryName(), e);
                }
                if (defineImpl) {
                    @SuppressWarnings("unchecked") Class<? extends EntityLivingBase> possessableClass =
                            PossessableEntityFactory.defineGenericPossessable((Class)entityEntry.getEntityClass());
                    EntityRegistry.EntityRegistration info = EntityRegistry.instance().lookupModSpawn(entityEntry.getEntityClass(), true);
                    int trackingRange, updateFrequency;
                    boolean sendVelocityUpdates;
                    if (info == null) {
                        // vanilla entities never have associated registration information
                        if (!entityEntry.getRegistryName().getResourceDomain().equals("minecraft")) {
                            Dissolution.LOGGER.info("No entity registration found for {}, using default values", entityEntry.getRegistryName());
                        }
                        trackingRange = 64;
                        updateFrequency = 1;
                        sendVelocityUpdates = true;
                    } else {
                        trackingRange = info.getTrackingRange();
                        updateFrequency = info.getUpdateFrequency();
                        sendVelocityUpdates = info.sendsVelocityUpdates();
                    }
                    // register the new class with the same characteristics as the original
                    event.getRegistry().register(
                            EntityEntryBuilder.create()
                                    .entity(possessableClass)
                                    .id(new ResourceLocation(Reference.MOD_ID, entityEntry.getRegistryName().getResourcePath() + "_possessable"), id++)
                                    .name(entityEntry.getName())
                                    .tracker(trackingRange, updateFrequency, sendVelocityUpdates)
                                    .build()
                    );
                }
            }
        }
    }

    /**
     * Just a workaround to suppress the exceptions that are normally caught by ConstructorFactory
     */
    private static Entity newInstance(EntityEntry entry) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        if (entityentry$factory == null) {
            entityentry$factory = EntityEntry.class.getDeclaredField("factory");
            entityentry$factory.setAccessible(true);
            contructorFactoryClass = Class.forName(EntityEntryBuilder.class.getName() + "$ConstructorFactory");
        }
        Class<?> factoryClass = entityentry$factory.get(entry).getClass();
        if (contructorFactoryClass.isAssignableFrom(factoryClass)) {
            return ReflectionHelper.findConstructor(entry.getEntityClass(), World.class).newInstance((Object) null);
        }
        return entry.newInstance(null);
    }

    @SubscribeEvent
    public static void lootLoad(LootTableLoadEvent event) {
        if (event.getName().toString().equals("minecraft:entities/villager")) {
            LootEntry entry = new LootEntryTable(new ResourceLocation("dissolution:inject/human"), 1, 1, new LootCondition[0], "dissolution_human");
            LootPool pool = new LootPool(new LootEntry[] {entry}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0), "dissolution_human");
            event.getTable().addPool(pool);
        }
    }

    @SubscribeEvent
    public static void addEntities(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> reg = event.getRegistry();
        // this entity is only there to allow debug features
        if (LadyLib.isDevEnv()) {
            registerEntity(reg, EntityPossessableImpl::new, "possessed_base", 64, true);
        }
        registerEntity(reg, EntityPlayerShell::new, "player_corpse", 64, true);
        LootTableList.register(EntityPlayerShell.LOOT);
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
        RenderingRegistry.registerEntityRenderingHandler(EntityPlayerShell.class, RenderPlayerCorpse::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPossessableImpl.class, renderManagerIn -> new RenderBiped<>(renderManagerIn, new ModelBiped(), 1f));
    }

}
