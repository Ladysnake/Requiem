/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.block.ObeliskRune;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.item.RequiemItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public class RequiemBlocks {
    public static final Block POLISHED_OBSIDIAN = new Block(AbstractBlock.Settings.copy(Blocks.OBSIDIAN));
    public static final Block POLISHED_OBSIDIAN_SLAB = new SlabBlock(AbstractBlock.Settings.copy(POLISHED_OBSIDIAN));
    public static final Block POLISHED_OBSIDIAN_STAIRS = new StairsBlock(POLISHED_OBSIDIAN.getDefaultState(), AbstractBlock.Settings.copy(POLISHED_OBSIDIAN)) {};   // anon class for protected constructor
    public static final RunicObsidianBlock RUNIC_OBSIDIAN_ATTRITION = new RunicObsidianBlock(AbstractBlock.Settings.copy(Blocks.OBSIDIAN), RequiemStatusEffects.ATTRITION, 3);
    public static final RunicObsidianBlock RUNIC_OBSIDIAN_EMANCIPATION = new RunicObsidianBlock(AbstractBlock.Settings.copy(Blocks.OBSIDIAN), RequiemStatusEffects.EMANCIPATION, 1);

    public static void init() {
        register(POLISHED_OBSIDIAN, "polished_obsidian");
        register(POLISHED_OBSIDIAN_SLAB, "polished_obsidian_slab");
        register(POLISHED_OBSIDIAN_STAIRS, "polished_obsidian_stairs");
        registerRunic(RUNIC_OBSIDIAN_ATTRITION, "runic_obsidian_attrition");
        registerRunic(RUNIC_OBSIDIAN_EMANCIPATION, "runic_obsidian_emancipation");
    }

    public static <T extends Block & ObeliskRune> void registerRunic(T block, String name) {
        register(block, name);
        ObeliskRune.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) -> block, block);
    }

    public static Block register(Block block, String name) {
        return register(block, name, true);
    }

    private static Block register(Block block, String name, boolean doItem) {
        Registry.register(Registry.BLOCK, Requiem.id(name), block);

        if (doItem) {
            BlockItem item = new BlockItem(block, new Item.Settings().group(ItemGroup.DECORATIONS));
            item.appendBlocks(Item.BLOCK_ITEMS, item);
            RequiemItems.registerItem(item, name);
        }

        return block;
    }

}
