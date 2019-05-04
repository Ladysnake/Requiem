package ladysnake.requiem.api.v1.util;

import ladysnake.requiem.api.v1.internal.ApiInternals;

import java.util.stream.Stream;

public interface SubDataManagerHelper {
    void registerSubDataManager(SubDataManager<?> manager);

    Stream<SubDataManager<?>> streamDataManagers();

    static SubDataManagerHelper getServerHelper() {
        return ApiInternals.getServerSubDataManagerHelper();
    }

    static SubDataManagerHelper getClientHelper() {
        return ApiInternals.getClientSubDataManagerHelper();
    }
}
