package ladysnake.dissolution.api.v1;

import ladysnake.dissolution.api.v1.internal.ApiInternals;
import org.apiguardian.api.API;

import java.util.stream.Stream;

public final class DissolutionApi {
    private DissolutionApi() { throw new AssertionError(); }

    @API(status = API.Status.EXPERIMENTAL)
    public static void registerPlugin(DissolutionPlugin entryPoint) {
        ApiInternals.registerPluginInternal(entryPoint);
    }

    @API(status = API.Status.EXPERIMENTAL)
    public static Stream<DissolutionPlugin> getRegisteredPlugins() {
        return ApiInternals.streamRegisteredPlugins();
    }

}
