package ladysnake.dissolution.common.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import ladysnake.dissolution.api.ISoulInteractable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class DissolutionConfigManager {

	public static Configuration config;
	private static final Class CONFIG_CLASS = DissolutionConfig.class;
	private static ImmutableSet<Class<? extends EntityMob>> TARGET_BLACKLIST;
	private static ImmutableSet<Pattern> GHOST_HUNTER_WHITELIST;
	private static ImmutableSet<Pattern> BLOCK_WHITELIST;
	private static Set<String> categoryNames;
	private static final String I18N_PREFIX = "config.dissolution";

	public enum FlightModes {
		CUSTOM_FLIGHT,
		CREATIVE_FLIGHT,
		SPECTATOR_FLIGHT,
		NO_FLIGHT
	}

	public enum EnforcedSoulStrength {
		NONE, STRONG, WEAK;

		public boolean getValue(boolean defaultStrength) {
			return (defaultStrength && (this == NONE)) || (this == STRONG);
		}
	}

	public static Set<String> getCategoryNames() {
		return categoryNames;
	}

	public static boolean isFlightSetTo(FlightModes flightMode) {
		return DissolutionConfig.ghost.flightMode == flightMode;
	}

	public static boolean isEntityBlacklistedFromMinionAttacks(EntityMob EntityIn) {
		return TARGET_BLACKLIST.contains(EntityIn.getClass());
	}

	@SuppressWarnings("ConstantConditions")
	public static boolean canEctoplasmInteractWith(Block block) {
		if(block instanceof ISoulInteractable)
			return true;
		String name = block.getRegistryName().toString();
		return !BLOCK_WHITELIST.isEmpty() && BLOCK_WHITELIST.stream().anyMatch(p -> p.matcher(name).matches());
	}

	public static boolean canEctoplasmBeAttackedBy(Entity entity) {
		if(entity instanceof ISoulInteractable)
			return true;
		if(GHOST_HUNTER_WHITELIST.isEmpty()) return false;
		Class<? extends Entity> entityClass = entity.getClass();
		EntityEntry entityEntry = EntityRegistry.getEntry(entityClass);
		return entityEntry != null && entityEntry.getRegistryName() != null &&
				GHOST_HUNTER_WHITELIST.stream().anyMatch(p -> p.matcher(entityEntry.getRegistryName().toString()).matches());
	}

	public static boolean canEctoplasmInteractWith(Item item) {
		return item == ModItems.DEBUG_ITEM;
	}

	/**
	 * Called once during the initialization phase to initialize the config system
	 * @param configFile the file in which the config information is stored
	 */
	public static void init(File configFile) {
		config = new Configuration(configFile, String.valueOf(Reference.CONFIG_VERSION));
		categoryNames = new TreeSet<>();
		updateConfig();
		load();
	}

	/**
	 * Called once during initialization and then every time the config needs to be reloaded
	 */
	private static void load() {
		readAndInitializeConfig(config);
		buildEctoplasmAttackWhitelist();
		buildMinionAttackBlacklist();
		buildBlockWhitelist();
	}

	/**
	 * Reads and initializes the configuration object associated with the config file
	 * @param config the configuration object to read and store values from
	 */
	private static void readAndInitializeConfig(Configuration config) {
		Field[] configFields = Arrays.stream(CONFIG_CLASS.getFields())
				.filter(field -> Modifier.isStatic(field.getModifiers())).toArray(Field[]::new);
		handleCategory(config, Configuration.CATEGORY_GENERAL, null, configFields);
		config.save();
	}

	/**
	 * Recursively reads a config category and initializes every contained field
	 * TODO Actually handle nested categories
	 * @param config a configuration object to read
	 * @param category this category's name, preceded by all its parents'
	 * @param categoryObject if this category isn't a static field, an object instance of it
	 * @param children an array of fields representing categories or properties that are children of this category
	 */
	private static void handleCategory(Configuration config, String category, Object categoryObject, Field[] children) {
		if(categoryObject != null) {
			ConfigCategory configCategory = config.getCategory(category);
			configCategory.setLanguageKey(I18N_PREFIX + "." + category);
			categoryNames.add(category);
		}
		for(Field child : children) {
			try {
				if(CONFIG_CLASS.equals(child.getType().getDeclaringClass())) {		// The child is a nested category
                    String subCategory = (categoryObject == null
						? "" : category + Configuration.CATEGORY_SPLITTER) + child.getName();
                    Object subCategoryObject = child.get(categoryObject);
                    Field[] subChildren = child.getType().getFields();
                    handleCategory(config, subCategory, subCategoryObject, subChildren);
                } else {														// The child is a property of this category
                    readAndAssignProperty(config, category, categoryObject, child);
                }
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Uses reflection to dynamically read and assign a field's value from the corresponding config property.
	 * If the value in the config file is invalid, it will be reset to the field's default value.
	 * Also generates comments and lang keys for every property.
	 * @param config the configuration object from which values are read
	 * @param category the category containing this property
	 * @param categoryObject the object that will be modified
	 * @param optionField the field of the object that will be assigned from the property
	 */
	private static void readAndAssignProperty(Configuration config, String category, Object categoryObject, Field optionField) {
		Property prop = null;
		try {
			String unlocalizedDesc = I18N_PREFIX + "." + category + "." + optionField.getName();
			if(optionField.getType().isArray())
				prop = handleArrayProperty(config, category, categoryObject, optionField);
			else
				prop = handleProperty(config, category, categoryObject, optionField);
			Config.Comment commentAnnotation = optionField.getAnnotation(Config.Comment.class);
			if (commentAnnotation != null)
				prop.setComment(commentAnnotation.value());
			prop.setLanguageKey(unlocalizedDesc);
			if(optionField.getAnnotation(Config.RequiresWorldRestart.class) != null)
				prop.setRequiresWorldRestart(true);
			if(optionField.getAnnotation(Config.RequiresMcRestart.class) != null)
				prop.setRequiresMcRestart(true);
			Config.RangeInt rangeInt = optionField.getAnnotation(Config.RangeInt.class);
			if(rangeInt != null) {
				prop.setMinValue(rangeInt.min());
				prop.setMaxValue(rangeInt.max());
			}
			Config.RangeDouble rangeDouble = optionField.getAnnotation(Config.RangeDouble.class);
			if(rangeDouble != null) {
				prop.setMinValue(rangeDouble.min());
				prop.setMaxValue(rangeDouble.max());
			}
		} catch (ReflectiveOperationException e) {
			Dissolution.LOGGER.error("Error while attempting to set a config property", e);
			try {
				if(prop != null)
					prop.set(String.valueOf(optionField.get(categoryObject)));
			} catch (IllegalAccessException e1) {
				Dissolution.LOGGER.error("Error while attempting to reset a config option", e1);
			}
		}
	}

	/**
	 * Handles a non-array config property.
	 * @throws ReflectiveOperationException if the operation failed somehow
	 */
	private static Property handleProperty(Configuration config, String category, Object categoryObject, Field optionField) throws ReflectiveOperationException {
		Property prop;
		Property.Type type = Property.Type.STRING;
		if(int.class.equals(optionField.getType()))
			type = Property.Type.INTEGER;
		else if(double.class.equals(optionField.getType()))
			type = Property.Type.DOUBLE;
		else if(boolean.class.equals(optionField.getType()))
			type = Property.Type.BOOLEAN;
		prop = config.get(category,	optionField.getName(), optionField.get(categoryObject).toString(),
				null, type);
		switch(prop.getType()) {
			case BOOLEAN: optionField.set(categoryObject, prop.getBoolean()); break;
			case INTEGER: optionField.set(categoryObject, prop.getInt()); break;
			case DOUBLE: optionField.set(categoryObject, prop.getDouble()); break;
			case STRING:
				if(String.class.equals(optionField.getType()))
					optionField.set(categoryObject, prop.getString());
				else if(Enum.class.equals(optionField.getType().getSuperclass())) {
					Method valueOf = optionField.getType().getMethod("valueOf", String.class);
					Method values = optionField.getType().getMethod("values");
					if(!prop.wasRead())
						prop.set(enumToString((Enum) optionField.get(categoryObject)));
					optionField.set(categoryObject, valueOf.invoke(null, stringToEnumName(prop.getString())));
					prop.setValidValues(Arrays.stream((Enum[])values.invoke(null)).map(DissolutionConfigManager::enumToString).toArray(String[]::new));
				} else Dissolution.LOGGER.warn("could not find type of field " + optionField.getName());
				break;
			default: Dissolution.LOGGER.warn("Why is there a property of type " + prop.getType() + " in my config ?");
		}
		return prop;
	}

	/**
	 * Handles reading and assignation of an array config property
	 * @throws IllegalAccessException if the field provided is inaccessible
	 */
	private static Property handleArrayProperty(Configuration config, String category, Object categoryObject, Field optionField) throws IllegalAccessException {
		Property prop;
		Property.Type type = Property.Type.STRING;
		if(int[].class.equals(optionField.getType()))
			type = Property.Type.INTEGER;
		else if(double[].class.equals(optionField.getType()))
			type = Property.Type.DOUBLE;
		else if(boolean[].class.equals(optionField.getType()))
			type = Property.Type.BOOLEAN;
		prop = config.get(category, optionField.getName(), (String[]) optionField.get(categoryObject),null, type);
		prop.setArrayEntryClass(GuiEditArrayEntries.StringEntry.class);
		switch (prop.getType()) {
			case BOOLEAN: optionField.set(categoryObject, prop.getBooleanList()); break;
			case INTEGER: optionField.set(categoryObject, prop.getIntList()); break;
			case DOUBLE: optionField.set(categoryObject, prop.getDoubleList()); break;
			case STRING: optionField.set(categoryObject, prop.getStringList()); break;
			default: Dissolution.LOGGER.warn("Why is there an array property of type " + prop.getType() + " in my config ?");
		}
		return prop;
	}

	/**
	 * Converts a user-friendly name to the internal corresponding enum name
	 */
	private static String stringToEnumName(String from) {
		return from.toUpperCase(Locale.ENGLISH).replace(' ', '_');
	}

	/**
	 * Converts an internal enum name to a more user-friendly name
	 */
	private static String enumToString(Enum from) {
		return from.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
	}

	/**
	 * If this config's last version is outdated, attempts to update defaults
	 */
	private static void updateConfig() {
	    // Updating configuration file to v3.0 (dissolution v0.5.2)
	    if(config.hasKey("don't touch that", "version")) {
	    	Dissolution.LOGGER.warn("Config file is out of date. Replacing with new format. A backup will be created.");
	    	resetConfig(config.getConfigFile());
	    }

		/*
		if(isBehind(config.getLoadedConfigVersion(), 3.1)) {
			// DO STUFF
		}
		*/

	    config.save();
	}

/*
	private static boolean isBehind(String compared, double reference) {
		try {
			return Double.compare(Double.parseDouble(compared), reference) < 0;
		} catch (NumberFormatException e) {
			Dissolution.LOGGER.warn("Someone tempered with the config's version number. Please don't do that.");
			return true;
		}
	}
*/

	private static void resetConfig(File configFile) {
		try {
			Files.copy(configFile, new File(configFile.getParent(), Reference.MOD_NAME + "_backup.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(configFile.delete()) {
			config = new Configuration(configFile, String.valueOf(Reference.CONFIG_VERSION));
		}
	}

	@SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(Reference.MOD_ID)) {
        	load();
        }
    }

	private static void buildMinionAttackBlacklist() {
		ImmutableSet.Builder<Class<? extends EntityMob>> builder = ImmutableSet.builder();
		builder.add(AbstractMinion.class);
		if (!DissolutionConfig.entities.minionsAttackCreepers)
			builder.add(EntityCreeper.class);
		TARGET_BLACKLIST = builder.build();
	}

	private static void buildEctoplasmAttackWhitelist() {
		ImmutableSet.Builder<Pattern> builder = ImmutableSet.builder();
		for(String entityName : DissolutionConfig.ghost.authorizedEntities)
			builder.add(Pattern.compile("^" + entityName + "$"));
		GHOST_HUNTER_WHITELIST = builder.build();
	}

	private static void buildBlockWhitelist() {
		ImmutableSet.Builder<Pattern> builder = ImmutableSet.builder();
		for (String blockName : DissolutionConfig.ghost.authorizedBlocks)
			builder.add(Pattern.compile("^" + blockName + "$"));
		BLOCK_WHITELIST = builder.build();
	}

}
