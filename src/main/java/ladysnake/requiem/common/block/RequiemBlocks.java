/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.common.block;

import com.google.common.base.Suppliers;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.block.ObeliskRune;
import ladysnake.requiem.api.v1.block.VagrantTargetableBlock;
import ladysnake.requiem.common.block.obelisk.InertRunestoneBlock;
import ladysnake.requiem.common.block.obelisk.ReclamationRunestoneBlock;
import ladysnake.requiem.common.block.obelisk.RiftRunestoneBlock;
import ladysnake.requiem.common.block.obelisk.RunestoneBlock;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.item.RequiemItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class RequiemBlocks {
    private static final Map<Block, BlockRegistration> allBlocks = new LinkedHashMap<>();

    public static final Block TACHYLITE = make(() -> new Block(AbstractBlock.Settings.copy(Blocks.OBSIDIAN)
        .hardness((Blocks.OBSIDIAN.getHardness() + Blocks.BASALT.getHardness()) / 2)
        .resistance((Blocks.OBSIDIAN.getBlastResistance() + Blocks.BASALT.getBlastResistance()) / 2)
    ), "tachylite/tachylite", ItemGroups.BUILDING_BLOCKS);
    public static final Block CHISELED_TACHYLITE = makeVariant(TACHYLITE, "tachylite/chiseled");
    public static final PillarBlock CHISELED_TACHYLITE_PILLAR = makePillar(CHISELED_TACHYLITE);
    public static final SlabBlock CHISELED_TACHYLITE_SLAB = makeSlab(CHISELED_TACHYLITE);
    public static final StairsBlock CHISELED_TACHYLITE_STAIRS = makeStairs(CHISELED_TACHYLITE);
    public static final SlabBlock TACHYLITE_SLAB = makeSlab(TACHYLITE);
    public static final StairsBlock TACHYLITE_STAIRS = makeStairs(TACHYLITE);
    public static final Block SCRAPED_TACHYLITE = makeVariant(TACHYLITE, "tachylite/scraped");
    public static final InertRunestoneBlock TACHYLITE_RUNESTONE = make(() -> new InertRunestoneBlock(AbstractBlock.Settings.copy(TACHYLITE)), "tachylite/runestone", ItemGroups.BUILDING_BLOCKS);
    public static final ExperienceDroppingBlock DERELICT_TACHYLITE = make(() -> new ExperienceDroppingBlock(AbstractBlock.Settings.copy(TACHYLITE), UniformIntProvider.create(7, 14)), "tachylite/derelict", ItemGroups.NATURAL);
    public static final RunestoneBlock RUNIC_TACHYLITE_ATTRITION = makeRunic("attrition", 3);
    public static final RunestoneBlock RUNIC_TACHYLITE_EMANCIPATION = makeRunic("emancipation", 1);
    public static final RunestoneBlock RUNIC_TACHYLITE_PENANCE = makeRunic("penance", 3);
    public static final ReclamationRunestoneBlock RUNIC_TACHYLITE_RECLAMATION = make(() -> new ReclamationRunestoneBlock(AbstractBlock.Settings.copy(TACHYLITE_RUNESTONE), () -> RequiemStatusEffects.RECLAMATION, 1), "tachylite/runic/reclamation", ItemGroups.FUNCTIONAL);
    public static final RiftRunestoneBlock RIFT_RUNE = make(() -> new RiftRunestoneBlock(AbstractBlock.Settings.copy(TACHYLITE_RUNESTONE)), "tachylite/runic/rift", ItemGroups.FUNCTIONAL);

    private static Block makeVariant(Block base, String id) {
        return make(() -> new Block(AbstractBlock.Settings.copy(base)), id, ItemGroups.BUILDING_BLOCKS);
    }

    private static PillarBlock makePillar(Block base) {
        return make(() -> new PillarBlock(AbstractBlock.Settings.copy(base)), allBlocks.get(base).name() + "_pillar", ItemGroups.BUILDING_BLOCKS);
    }

    private static StairsBlock makeStairs(Block base) {
        return make(() -> new StairsBlock(base.getDefaultState(), AbstractBlock.Settings.copy(base)), allBlocks.get(base).name() + "_stairs", ItemGroups.BUILDING_BLOCKS);
    }

    private static SlabBlock makeSlab(Block base) {
        return make(() -> new SlabBlock(AbstractBlock.Settings.copy(base)), allBlocks.get(base).name() + "_slab", ItemGroups.BUILDING_BLOCKS);
    }

    private static RunestoneBlock makeRunic(String effectName, int maxLevel) {
        return make(() -> new RunestoneBlock(
            AbstractBlock.Settings.copy(TACHYLITE),
            Suppliers.memoize(() -> Registries.STATUS_EFFECT.getOrEmpty(Requiem.id(effectName)).orElseThrow()),
            maxLevel
        ), "tachylite/runic/" + effectName, ItemGroups.FUNCTIONAL);
    }

    private static <B extends Block> B make(Supplier<B> factory, String name, ItemGroup itemGroup) {
        B ret = factory.get();
        allBlocks.put(ret, new BlockRegistration(name, new BlockRegistration.BlockItemRegistration(itemGroup, BlockItem::new)));
        return ret;
    }

    public static void init() {
        allBlocks.forEach(RequiemBlocks::register);
        InertRunestoneBlock.registerCallbacks();
        VagrantTargetableBlock.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) -> state.get(InertRunestoneBlock.ACTIVATED) ? RIFT_RUNE : null, RIFT_RUNE);
    }

    public static Stream<Map.Entry<Block, Identifier>> streamRunestones() {
        return allBlocks.entrySet().stream().filter(e -> e.getKey() instanceof InertRunestoneBlock).map(e -> Map.entry(e.getKey(), Requiem.id(e.getValue().name())));
    }

    public static void register(Block block, String name, ItemGroup itemGroup) {
        register(block, new BlockRegistration(name, new BlockRegistration.BlockItemRegistration(itemGroup, BlockItem::new)));
    }

    private static void register(Block block, BlockRegistration registration) {
        Registry.register(Registries.BLOCK, Requiem.id(registration.name()), block);

        BlockRegistration.BlockItemRegistration blockItemRegistration = registration.blockItemRegistration();
        if (blockItemRegistration != null) {
            BlockItem item = blockItemRegistration.blockItemFactory().apply(block, new Item.Settings());
            item.appendBlocks(Item.BLOCK_ITEMS, item);
            RequiemItems.registerItem(item, registration.name(), blockItemRegistration.group());
        }

        if (block instanceof ObeliskRune rune) {
            ObeliskRune.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) -> rune, block);
        }
    }
}
