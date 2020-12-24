/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.client.render;

import ladysnake.requiem.Requiem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CuredEntityTexture extends ResourceTexture {
    private static final Map<Identifier, Identifier> filteredTextures = new HashMap<>();

    private final Identifier sourceLocation;

    public static Identifier from(Identifier sourceLocation) {
        Identifier cached = filteredTextures.get(sourceLocation);
        if (cached != null) return cached;

        String path = sourceLocation.getPath();
        int idx = path.lastIndexOf('/', path.lastIndexOf('/'));
        String newPath = path.substring(0, idx) + "/cured/" + sourceLocation.getNamespace() + path.substring(idx);
        Identifier filteredLocation = Requiem.id(newPath);
        CuredEntityTexture filteredTexture = new CuredEntityTexture(filteredLocation, sourceLocation);
        MinecraftClient.getInstance().getTextureManager().registerTexture(filteredLocation, filteredTexture);
        filteredTextures.put(sourceLocation, filteredLocation);
        return filteredLocation;
    }

    public CuredEntityTexture(Identifier location, Identifier sourceLocation) {
        super(location);
        this.sourceLocation = sourceLocation;
    }

    @Override
    protected TextureData loadTextureData(ResourceManager resourceManager) {
        TextureData textureData = super.loadTextureData(resourceManager);
        try {
            textureData.checkException();
        } catch (IOException e) {
            try {
                return this.loadFilteredTexture(resourceManager);
            } catch (IOException e1) {
                Requiem.LOGGER.error("Failed to edit texture data", e1);
            }
        }
        return textureData;
    }

    protected TextureData loadFilteredTexture(ResourceManager resourceManager) throws IOException {
        TextureData textureData = ResourceTexture.TextureData.load(resourceManager, this.sourceLocation);
        NativeImage image = textureData.getImage();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getPixelColor(x, y) == 0xFFFFFFFF) {
                    image.setPixelColor(x, y, 0xFFFFAA66);
                }
            }
        }
        return textureData;
    }
}
