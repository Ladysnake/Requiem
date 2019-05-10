/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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

import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CommonSubDataManagerHelper implements SubDataManagerHelper {
    private List<SubDataManager<?>> managers = new ArrayList<>();

    @Override
    public void registerSubDataManager(SubDataManager<?> manager) {
        this.managers.add(manager);
    }

    @Override
    public Stream<SubDataManager<?>> streamDataManagers() {
        return this.managers.stream();
    }
}
