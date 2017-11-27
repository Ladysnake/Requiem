package ladysnake.dissolution.common.config;

import ladysnake.dissolution.client.proxy.ClientProxy;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.networking.ConfigMessage;
import ladysnake.dissolution.common.networking.ConfigPacket;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigTests {

    @Before
    public void init() {
        try {
            Field minecraftHome = FMLInjectionData.class.getDeclaredField("minecraftHome");
            minecraftHome.setAccessible(true);
            minecraftHome.set(null, new File("run"));
            Dissolution.proxy = new ClientProxy();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        ((Logger) Dissolution.LOGGER).setLevel(Level.DEBUG);
    }

    @Test
    public void testConfigLocation() {
        assertEquals(new File("run"), FMLInjectionData.data()[6]);
    }

    @Test
    public void testConfig() {
        DissolutionConfigManager.init(new File("run/config/dissolution.cfg"));
        assertNotNull(DissolutionConfigManager.config);
        DissolutionConfigManager.getRootCategories().stream().map(this::convert).forEach(System.out::println);
        DissolutionConfigManager.syncedProps.forEach((s, p) -> System.out.println(s + "=" + convert(p)));
    }

    @Test
    public void testConfigPacket() {
        DissolutionConfigManager.init(new File("run/config/dissolution.cfg"));
        assertEquals(DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT, Dissolution.config.ghost.flightMode);
        ConfigMessage message = new ConfigMessage(DissolutionConfigManager.syncedProps.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue().getString())));
        DissolutionConfigManager.init(new File("run/config/dissolutiontest.cfg"));
        assertEquals(DissolutionConfigManager.FlightModes.CREATIVE_FLIGHT, Dissolution.config.ghost.flightMode);
        new ConfigPacket().syncConfig(message);
        assertEquals(DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT, Dissolution.config.ghost.flightMode);
    }

    @Test
    public void testRestore() {
        DissolutionConfigManager.init(new File("run/config/dissolution.cfg"));
        ConfigMessage message = new ConfigMessage(DissolutionConfigManager.syncedProps.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue().getString())));
        DissolutionConfigManager.init(new File("run/config/dissolutiontest.cfg"));
        DissolutionConfigManager.onClientDisconnect(null);
        new ConfigPacket().syncConfig(message);
        assertEquals(DissolutionConfigManager.FlightModes.CREATIVE_FLIGHT, Dissolution.config.ghost.flightMode);
    }

    private String convert(ConfigCategory cat) {
        StringBuilder ret = new StringBuilder(cat.getQualifiedName());
        ret.append(" {\n");
        for (ConfigCategory child : cat.getChildren()) {
            ret.append(convert(child)).append("\n");
        }
        cat.forEach((key, value) -> {
            ret.append(convert(value));
            if (DissolutionConfigManager.syncedProps.containsKey(key))
                ret.append(" [synced]");
            ret.append("\n");
        });
        ret.append("}");
        return ret.toString();
    }

    private String convert(Property prop) {
        return prop.getName() + " -> " + prop.getString() + " (" + prop.getType().toString().toLowerCase() + ")";
    }

}
