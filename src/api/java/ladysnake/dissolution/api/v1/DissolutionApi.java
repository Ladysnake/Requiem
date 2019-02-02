package ladysnake.dissolution.api.v1;

import ladysnake.dissolution.api.v1.annotation.AccessedThroughReflection;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class DissolutionApi {
    private DissolutionApi() { throw new AssertionError(); }

    private static List<DissolutionPlugin> plugins = new ArrayList<>();

    @AccessedThroughReflection
    private static Consumer<DissolutionPlugin> registerHandler = plugins::add;

    @API(status = API.Status.EXPERIMENTAL)
    public static void registerPlugin(DissolutionPlugin entryPoint) {
        registerHandler.accept(entryPoint);
    }

    @API(status = API.Status.EXPERIMENTAL)
    public static Stream<DissolutionPlugin> getRegisteredPlugins() {
        return plugins.stream();
    }
}
