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

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class SimpleUnbakedModel implements UnbakedModel {
    Function<ModelBuilder, BakedModel> baker;
    private final Set<SpriteIdentifier> textureDependencies;

    public SimpleUnbakedModel(Function<ModelBuilder, BakedModel> baker, Set<SpriteIdentifier> textureDependencies) {
        this.baker = baker;
        this.textureDependencies = textureDependencies;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> var1, Set<Pair<String, String>> set) {
        return textureDependencies;
    }

    @Override
    public BakedModel bake(ModelLoader modelLoader, Function<SpriteIdentifier, Sprite> spriteFunc, ModelBakeSettings var3, Identifier var4) {
        return baker.apply(ModelBuilder.prepare(spriteFunc));
    }
}
