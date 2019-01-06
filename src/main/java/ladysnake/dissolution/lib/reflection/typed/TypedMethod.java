package ladysnake.dissolution.lib.reflection.typed;

import org.apiguardian.api.API;

import java.lang.invoke.MethodHandle;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * A typed method handle, typically with an <code>invoke</code> method
 * that has a variable number of typed parameters
 *
 * @since 2.6
 */
@API(status = EXPERIMENTAL, since = "2.6.2")
public abstract class TypedMethod {
    protected MethodHandle methodHandle;
    protected String name;

    protected TypedMethod(MethodHandle methodHandle, String name) {
        this.methodHandle = methodHandle;
        this.name = name;
    }

}
