package ladysnake.dissolution.common.impl.possession.asm;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * A class loader allowing the creation of any class from its bytecode, as well as its injection into the classpath
 */
@API(status = INTERNAL)
public class AsmClassLoader extends ClassLoader {
    AsmClassLoader() {
        super(AsmClassLoader.class.getClassLoader());
    }

    public Class<?> define(String name, byte[] data) {
        return defineClass(name, data, 0, data.length);
    }
}
