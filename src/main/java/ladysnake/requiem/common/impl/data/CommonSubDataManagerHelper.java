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
