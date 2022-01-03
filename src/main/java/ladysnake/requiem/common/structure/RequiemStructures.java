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
package ladysnake.requiem.common.structure;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.tag.Tag;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public final class RequiemStructures {
    public static final Tag<Biome> SOUL_SAND_VALLEYS = TagFactory.BIOME.create(Requiem.id("derelict_obelisk_locations"));
    public static final StructureFeature<DefaultFeatureConfig> DERELICT_OBELISK = new DerelictObeliskFeature(DefaultFeatureConfig.CODEC);
    public static final ConfiguredStructureFeature<?, ?> CONFIGURED_DERELICT_OBELISK = DERELICT_OBELISK.configure(DefaultFeatureConfig.INSTANCE);

    public static void init() {
        FabricStructureBuilder.create(Requiem.id("derelict_obelisk"), DERELICT_OBELISK)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(new StructureConfig(10, 5, 82692722))
            .register();
        Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, Requiem.id("configured_derelict_obelisk"), CONFIGURED_DERELICT_OBELISK);
        BiomeModifications.create(Requiem.id("derelict_obelisk_addition"))
            .add(
                ModificationPhase.ADDITIONS,
                BiomeSelectors.tag(SOUL_SAND_VALLEYS),
                context -> context.getGenerationSettings().addBuiltInStructure(CONFIGURED_DERELICT_OBELISK)
            );
    }
}
