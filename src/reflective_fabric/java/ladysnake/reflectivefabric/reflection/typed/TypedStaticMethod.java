package ladysnake.reflectivefabric.reflection.typed;

import java.lang.invoke.MethodHandle;

public class TypedStaticMethod extends TypedMethod {
    protected Class<?> declaringClass;

    protected TypedStaticMethod(MethodHandle methodHandle, String name, Class<?> declaringClass) {
        super(methodHandle, name);
        this.declaringClass = declaringClass;
    }
}
