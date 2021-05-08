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
package ladysnake.requiem.common.impl.possession.item;

import com.google.gson.JsonParseException;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.util.SubDataManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PossessionItemUseManager implements SubDataManager<List<PossessionItemUse>> {
    public static final Identifier LISTENER_ID = Requiem.id("mob_items");

    private final List<PossessionItemUse> uses = new ArrayList<>();

    @Override
    public void apply(List<PossessionItemUse> data) {
        this.uses.clear();
        this.uses.addAll(data);
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        buf.writeVarInt(this.uses.size());
        for (PossessionItemUse use : this.uses) {
            use.toPacket(buf);
        }
    }

    @Override
    public List<PossessionItemUse> loadFromPacket(PacketByteBuf buf) {
        List<PossessionItemUse> ret = new ArrayList<>();
        int nbUses = buf.readVarInt();
        for (int i = 0; i < nbUses; i++) {
            ret.add(PossessionItemUse.fromPacket(buf));
        }
        return ret;
    }

    @Override
    public CompletableFuture<List<PossessionItemUse>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            List<PossessionItemUse> itemUses = new ArrayList<>();
            for (Identifier location : manager.findResources("requiem_mob_items", (res) -> res.endsWith(".json"))) {
                try (Resource res = manager.getResource(location); Reader in = new InputStreamReader(res.getInputStream())) {
                    itemUses.add(PossessionItemUse.deserialize(JsonHelper.deserialize(in)));
                } catch (IOException | JsonParseException e) {
                    Requiem.LOGGER.error("[Requiem] Could not read mob items data from {}", location, e);
                }
            }
            Collections.sort(itemUses);
            return itemUses;
        }, executor);
    }

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }
}
