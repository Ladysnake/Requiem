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
 */
package ladysnake.requiem.common.impl.data;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.util.SubDataManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class ServerSubDataManagerHelper extends CommonSubDataManagerHelper {

    @Override
    public void registerSubDataManager(SubDataManager<?> serverManager) {
        super.registerSubDataManager(serverManager);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(serverManager);
        Requiem.LOGGER.info("[Requiem] Registered sub data manager {}", serverManager);
    }
}
