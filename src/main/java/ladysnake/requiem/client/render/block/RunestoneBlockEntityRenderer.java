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
package ladysnake.requiem.client.render.block;

import com.mojang.blaze3d.vertex.VertexFormats;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.block.RequiemBlocks;
import ladysnake.requiem.common.block.obelisk.InertRunestoneBlock;
import ladysnake.requiem.common.block.obelisk.RunestoneBlockEntity;
import ladysnake.satin.api.managed.ManagedCoreShader;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.Registry;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RunestoneBlockEntityRenderer implements BlockEntityRenderer<RunestoneBlockEntity> {
    private static final ManagedCoreShader SHADER = ShaderEffectManager.getInstance().manageCoreShader(Requiem.id("rendertype_obelisk_rune"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);

    private final BlockRenderManager blockRenderManager;
    private final Map<Block, Identifier> runeModelIds;

    public RunestoneBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.blockRenderManager = context.getRenderManager();
        this.runeModelIds = RequiemBlocks.streamRunestones().map(e -> Map.entry(e.getKey(), createRuneIdentifier(e.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (i1, i2) -> {
            throw new IllegalStateException();
        }, IdentityHashMap::new));
    }

    public static Identifier createRuneIdentifier(Identifier runestoneId) {
        if (runestoneId.equals(Registry.BLOCK.getId(RequiemBlocks.TACHYLITE_RUNESTONE))) {
            return Requiem.id("tachylite_rune/neutral");
        }
        return new Identifier(
            runestoneId.getNamespace(),
            "tachylite_rune/" + runestoneId.getPath().substring(runestoneId.getPath().lastIndexOf('/') + 1));
    }

    @Override
    public void render(RunestoneBlockEntity runestone, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState blockState = runestone.getCachedState();
        if (!blockState.get(InertRunestoneBlock.ACTIVATED)) return;
        BlockPos pos = runestone.getPos();
        int powerRate = Math.round(runestone.getPowerRate(tickDelta) * 100);
        this.blockRenderManager
            .getModelRenderer()
            .render(
                runestone.getWorld(),
                BakedModelManagerHelper.getModel(this.blockRenderManager.getModels().getModelManager(), this.runeModelIds.get(blockState.getBlock())),
                blockState,
                pos,
                matrices,
                vertexConsumers.getBuffer(SHADER.getRenderLayer(RenderLayer.getCutoutMipped())),
                false,
                RandomGenerator.createLegacy(),
                blockState.getRenderingSeed(pos),
                OverlayTexture.packUv(powerRate, 0)
            );
    }
}
