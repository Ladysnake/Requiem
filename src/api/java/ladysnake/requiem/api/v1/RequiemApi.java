package ladysnake.requiem.api.v1;

import ladysnake.requiem.api.v1.internal.ApiInternals;
import org.apiguardian.api.API;

import java.util.stream.Stream;

public final class RequiemApi {
    private RequiemApi() { throw new AssertionError(); }

    @API(status = API.Status.EXPERIMENTAL)
    public static void registerPlugin(RequiemPlugin entryPoint) {
        ApiInternals.registerPluginInternal(entryPoint);
    }

    @API(status = API.Status.EXPERIMENTAL)
    public static Stream<RequiemPlugin> getRegisteredPlugins() {
        return ApiInternals.streamRegisteredPlugins();
    }

}
