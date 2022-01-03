/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.pandemonium.common;

import blue.endless.jankson.Comment;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.SettingNamingConvention;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import ladysnake.requiem.Requiem;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PandemoniumConfig {
    public static final PossessConfig possession = new PossessConfig();

    public static class PossessConfig {
        @Comment("Toggles whether all mobs can be possessed by default. Individual mobs can still be configured through datapacks.")
        public boolean allowPossessingAllMobs = false;
    }

    private static final AnnotatedSettings settings = AnnotatedSettings.builder()
        .useNamingConvention(SettingNamingConvention.SNAKE_CASE)
        .build();
    private static final ConfigBranch configTree = ConfigTree.builder()
        .fork("possession").applyFromPojo(possession, settings).finishBranch()
        .build();

    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("pandemonium.json5");
    private static final JanksonValueSerializer serializer = new JanksonValueSerializer(false);

    public static ConfigBranch configTree() {
        return configTree;
    }

    public static void load() {
        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                FiberSerialization.deserialize(configTree, in, serializer);
            } catch (IOException | ValueDeserializationException e) {
                Requiem.LOGGER.error("[Pandemonium] Failed to load config", e);
            }
        }
        PandemoniumConfig.save();
    }

    public static void save() {
        try (OutputStream out = Files.newOutputStream(configPath)) {
            FiberSerialization.serialize(configTree, out, serializer);
        } catch (IOException e) {
            Requiem.LOGGER.error("[Pandemonium] Failed to save config", e);
        }
    }
}
