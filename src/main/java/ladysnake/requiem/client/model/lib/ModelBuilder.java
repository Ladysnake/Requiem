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

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.Objects;
import java.util.function.Function;

public final class ModelBuilder {

    private static ModelBuilder instance;

    private static Function<SpriteIdentifier, Sprite> spriteFunc;

    public static ModelBuilder prepare(Function<SpriteIdentifier, Sprite> spriteFuncIn) {
        if(instance == null) {
            instance = new ModelBuilder(Objects.requireNonNull(RendererAccess.INSTANCE.getRenderer()));
        }
        spriteFunc = spriteFuncIn;
        return instance;
    }

    public final MeshBuilder builder;
    private final MaterialFinder finder;
    public static final int FULL_BRIGHTNESS = 15 << 20 | 15 << 4;

    private ModelBuilder(Renderer renderer) {
        builder = renderer.meshBuilder();
        finder = renderer.materialFinder();
    }

    public MaterialFinder finder() {
        return finder.clear();
    }

    public Sprite getSprite(String spriteName) {
        return spriteFunc.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(spriteName)));
    }

    public void box(
        RenderMaterial material,
        int color, Function<Direction, Sprite> sprites,
        float minX, float minY, float minZ,
        float maxX, float maxY, float maxZ) {

        builder.getEmitter()
            .material(material)
            .square(Direction.UP, minX, minZ, maxX, maxZ, 1-maxY)
            .spriteColor(0, color, color, color, color)
            .spriteUnitSquare(0)
            .spriteBake(0, sprites.apply(Direction.UP), MutableQuadView.BAKE_NORMALIZED)
            .emit()

            .material(material)
            .square(Direction.DOWN, minX, minZ, maxX, maxZ, minY)
            .spriteColor(0, color, color, color, color)
            .spriteUnitSquare(0)
            .spriteBake(0, sprites.apply(Direction.DOWN), MutableQuadView.BAKE_NORMALIZED)
            .emit()

            .material(material)
            .square(Direction.EAST, minZ, minY, maxZ, maxY, 1-maxX)
            .spriteColor(0, color, color, color, color)
            .spriteUnitSquare(0)
            .spriteBake(0, sprites.apply(Direction.EAST), MutableQuadView.BAKE_NORMALIZED)
            .emit()

            .material(material)
            .square(Direction.WEST, minZ, minY, maxZ, maxY, minX)
            .spriteColor(0, color, color, color, color)
            .spriteUnitSquare(0)
            .spriteBake(0, sprites.apply(Direction.WEST), MutableQuadView.BAKE_NORMALIZED)
            .emit()

            .material(material)
            .square(Direction.SOUTH, minX, minY, maxX, maxY, 1-maxZ)
            .spriteColor(0, color, color, color, color)
            .spriteUnitSquare(0)
            .spriteBake(0, sprites.apply(Direction.SOUTH), MutableQuadView.BAKE_NORMALIZED)
            .emit()

            .material(material)
            .square(Direction.NORTH, minX, minY, maxX, maxY, minZ)
            .spriteColor(0, color, color, color, color)
            .spriteUnitSquare(0)
            .spriteBake(0, sprites.apply(Direction.NORTH), MutableQuadView.BAKE_NORMALIZED)
            .emit();
    }
}
