/* ******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * *****************************************************************************/

package ladysnake.requiem.client.model.lib;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class SimpleBakedModel extends AbstractBakedModel {
    protected final Mesh mesh;
    protected final Supplier<MeshTransformer> transformerFactory;
    protected WeakReference<List<BakedQuad>[]> quadLists = null;
    protected final ItemProxy itemProxy = new ItemProxy();

    public SimpleBakedModel(Mesh mesh, ModelTransformation transformation, Sprite sprite, @Nullable Supplier<MeshTransformer> transformerFactory) {
        super(sprite, transformation);
        this.mesh = mesh;
        this.transformerFactory = transformerFactory;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random rand) {
        List<BakedQuad>[] lists = quadLists == null ? null : quadLists.get();
        if(lists == null) {
            lists = ModelHelper.toQuadLists(mesh);
            quadLists = new WeakReference<>(lists);
        }
        final List<BakedQuad> result = lists[face == null ? 6 : face.getId()];
        return result == null ? List.of() : result;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        final MeshTransformer transform = transformerFactory == null ? null : transformerFactory.get().prepare(blockView, state, pos, randomSupplier);
        if(transform != null) {
            context.pushTransform(transform);
        }
        if(mesh != null) {
            context.meshConsumer().accept(mesh);
        }
        if(transform != null) {
            context.popTransform();
        }
    }

    @Override
    public ModelOverrideList getOverrides() {
        return itemProxy;
    }

    protected class ItemProxy extends ModelOverrideList {
        public ItemProxy() {
            super(null, null, null, List.of());
        }

        @Override
        public BakedModel apply(BakedModel bakedModel, ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int seed) {
            return SimpleBakedModel.this;
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        final RenderContext.QuadTransform transform = transformerFactory == null ? null : transformerFactory.get().prepare(stack, randomSupplier);
        if(transform != null) {
            context.pushTransform(transform);
        }
        if(mesh != null) {
            context.meshConsumer().accept(mesh);
        }
        if(transform != null) {
            context.popTransform();
        }
    }
}
