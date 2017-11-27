package ladysnake.dissolution.common.config;

import ladysnake.dissolution.common.Dissolution;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;

/**
 * A class that does mostly the same things as {@link net.minecraftforge.common.config.ConfigManager} but with a few tweaks
 * As most of the utility classes in forge are private, everything has been recoded from scratch as an exercise, more or less efficiently
 */
public class DissolutionConfigReader {
    private static final Class CONFIG_CLASS = DissolutionConfig.class;
    private static final String I18N_PREFIX = "config.dissolution";

    /**
     * Reads and initializes the configuration object associated with the config file
     * The config object needs to be saved afterwards if needed
     * @param config the configuration object to read and store values from
     */
    static void readAndInitializeConfig(Configuration config) {
        Field[] configFields = Arrays.stream(CONFIG_CLASS.getFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers())).toArray(Field[]::new);
        handleCategory(config, new ConfigCategory(Configuration.CATEGORY_GENERAL), Dissolution.config, configFields);
    }

    /**
     * Recursively reads a config category and initializes every contained field
     *
     * @param config   a configuration object to read
     * @param category this category's name, preceded by all its parents'
     * @param instance if this category isn't a static field, an object instance of it
     * @param children an array of fields representing categories or properties that are children of this category
     */
    private static void handleCategory(Configuration config, ConfigCategory category, Object instance, Field[] children) {
        boolean isRoot = DissolutionConfig.class.equals(instance.getClass());
        category.setLanguageKey(I18N_PREFIX + "." + category.getQualifiedName());
        if (!category.isChild())
            DissolutionConfigManager.rootCategories.add(category);
        for (Field child : children) {
            try {
                if (instance.getClass().equals(child.getType().getDeclaringClass())) {        // The child is a nested category
                    ConfigCategory subCategory = new ConfigCategory(child.getName(), isRoot ? null : category);
                    Object subCategoryObject = child.get(instance);
                    Field[] subChildren = child.getType().getFields();
                    handleCategory(config, subCategory, subCategoryObject, subChildren);
                } else {                                                        // The child is a property of this category
                    readAndAssignProperty(config, category, instance, child);
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
     *
     * @param config         the configuration object from which values are read
     * @param category       the category containing this property
     * @param categoryObject the object that will be modified
     * @param optionField    the field of the object that will be assigned from the property
     */
    private static void readAndAssignProperty(Configuration config, ConfigCategory category, Object categoryObject, Field optionField) {
        Property prop = null;
        try {
            String unlocalizedDesc = I18N_PREFIX + "." + category + "." + optionField.getName();
            if (optionField.getType().isArray())
                prop = handleArrayProperty(config, category, categoryObject, optionField);
            else
                prop = handleProperty(config, category, categoryObject, optionField);
            category.put(optionField.getName(), prop);
            Sync syncAnnotation = optionField.getAnnotation(Sync.class);
            if(syncAnnotation != null) {
                DissolutionConfigManager.syncedProps.put(optionField.getName(), prop);
            }
            Config.Comment commentAnnotation = optionField.getAnnotation(Config.Comment.class);
            if (commentAnnotation != null)
                prop.setComment(commentAnnotation.value()[0]);
            prop.setLanguageKey(unlocalizedDesc);
            if (optionField.getAnnotation(Config.RequiresWorldRestart.class) != null)
                prop.setRequiresWorldRestart(true);
            if (optionField.getAnnotation(Config.RequiresMcRestart.class) != null)
                prop.setRequiresMcRestart(true);
            Config.RangeInt rangeInt = optionField.getAnnotation(Config.RangeInt.class);
            if (rangeInt != null) {
                prop.setMinValue(rangeInt.min());
                prop.setMaxValue(rangeInt.max());
            }
            Config.RangeDouble rangeDouble = optionField.getAnnotation(Config.RangeDouble.class);
            if (rangeDouble != null) {
                prop.setMinValue(rangeDouble.min());
                prop.setMaxValue(rangeDouble.max());
            }
        } catch (ReflectiveOperationException e) {
            Dissolution.LOGGER.error("Error while attempting to set a config property", e);
            try {
                if (prop != null)
                    prop.set(String.valueOf(optionField.get(categoryObject)));
            } catch (IllegalAccessException e1) {
                Dissolution.LOGGER.error("Error while attempting to reset a config option", e1);
            }
        }
    }

    /**
     * Handles a non-array config property.
     *
     * @throws ReflectiveOperationException if the operation failed somehow
     */
    private static Property handleProperty(Configuration config, ConfigCategory category, Object categoryObject, Field optionField) throws ReflectiveOperationException {
        Property prop;
        Property.Type type = Property.Type.STRING;
        if (int.class.equals(optionField.getType()))
            type = Property.Type.INTEGER;
        else if (double.class.equals(optionField.getType()))
            type = Property.Type.DOUBLE;
        else if (boolean.class.equals(optionField.getType()))
            type = Property.Type.BOOLEAN;
        prop = config.get(category.getQualifiedName(), optionField.getName(), optionField.get(categoryObject).toString(),
                null, type);
        switch (prop.getType()) {
            case BOOLEAN:
                optionField.set(categoryObject, prop.getBoolean());
                break;
            case INTEGER:
                optionField.set(categoryObject, prop.getInt());
                break;
            case DOUBLE:
                optionField.set(categoryObject, prop.getDouble());
                break;
            case STRING:
                if (String.class.equals(optionField.getType()))
                    optionField.set(categoryObject, prop.getString());
                else if (Enum.class.equals(optionField.getType().getSuperclass())) {
                    Method valueOf = optionField.getType().getMethod("valueOf", String.class);
                    Method values = optionField.getType().getMethod("values");
                    if (!prop.wasRead())
                        prop.set(enumToString((Enum) optionField.get(categoryObject)));
                    optionField.set(categoryObject, valueOf.invoke(null, stringToEnumName(prop.getString())));
                    prop.setValidValues(Arrays.stream((Enum[]) values.invoke(null)).map(DissolutionConfigReader::enumToString).toArray(String[]::new));
                } else Dissolution.LOGGER.warn("Could not find type of field " + optionField.getName());
                break;
            default:
                Dissolution.LOGGER.warn("Why is there a property of type " + prop.getType() + " in my config ?");
        }
        return prop;
    }

    /**
     * Handles reading and assignation of an array config property
     *
     * @throws IllegalAccessException if the field provided is inaccessible
     */
    private static Property handleArrayProperty(Configuration config, ConfigCategory category, Object categoryObject, Field optionField) throws IllegalAccessException {
        Property prop;
        Property.Type type = Property.Type.STRING;
        if (int[].class.equals(optionField.getType()))
            type = Property.Type.INTEGER;
        else if (double[].class.equals(optionField.getType()))
            type = Property.Type.DOUBLE;
        else if (boolean[].class.equals(optionField.getType()))
            type = Property.Type.BOOLEAN;
        prop = config.get(category.getQualifiedName(), optionField.getName(), (String[]) optionField.get(categoryObject), null, type);
        if (Dissolution.proxy.getSide() == Side.CLIENT)
            prop.setArrayEntryClass(GuiEditArrayEntries.StringEntry.class);
        switch (prop.getType()) {
            case BOOLEAN:
                optionField.set(categoryObject, prop.getBooleanList());
                break;
            case INTEGER:
                optionField.set(categoryObject, prop.getIntList());
                break;
            case DOUBLE:
                optionField.set(categoryObject, prop.getDoubleList());
                break;
            case STRING:
                optionField.set(categoryObject, prop.getStringList());
                break;
            default:
                Dissolution.LOGGER.warn("Why is there an array property of type " + prop.getType() + " in my config ?");
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
}
