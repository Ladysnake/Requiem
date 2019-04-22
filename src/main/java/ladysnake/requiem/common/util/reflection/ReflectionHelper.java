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
package ladysnake.requiem.common.util.reflection;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import org.apiguardian.api.API;
import org.objectweb.asm.Type;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;

public class ReflectionHelper {

    private static MethodHandles.Lookup TRUSTED_LOOKUP;

    public static final String INTERMEDIARY = "intermediary";

    @API(status = MAINTAINED, since = "1.0.0")
    public static boolean isDevEnv() {
        return FabricLauncherBase.getLauncher().isDevelopment();
    }

    @API(status = MAINTAINED, since = "1.0.0")
    public static String getMethodDescriptor(Class<?> returnType, Class<?>... parameterTypes) {
        return Type.getMethodDescriptor(Type.getType(returnType), Arrays.stream(parameterTypes).map(Type::getType).toArray(Type[]::new));
    }

    @API(status = MAINTAINED, since = "1.0.0")
    public static String getFieldDescriptor(Class<?> type) {
        return Type.getType(type).getDescriptor();
    }

    @API(status = STABLE, since = "1.0.0")
    public static Class<?> findClass(String obfClassName) throws ClassNotFoundException {
        return Class.forName(getMappingResolver().mapClassName(INTERMEDIARY, obfClassName));
    }

    /**
     * Finds a method with the specified Intermediary name and parameters in the given class and generates a {@link MethodHandle method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param parameterTypes The parameter types of the method to find.
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     */
    @API(status = MAINTAINED, since = "1.0.0")
    public static MethodHandle findMethodHandleFromObfName(Class<?> clazz, String methodObfName, Class<?> returnType, Class<?>... parameterTypes) throws UnableToFindMethodException {
        try {
            String methodDesc = getMethodDescriptor(returnType, parameterTypes);
            String deobfName = getMappingResolver().mapMethodName(INTERMEDIARY, clazz.getName(), methodObfName, methodDesc);
            Method m = clazz.getDeclaredMethod(deobfName, parameterTypes);
            return MethodHandles.lookup().unreflect(m);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new UnableToFindMethodException(e);
        }
    }

    /**
     * Finds a field with the specified SRG name and type in the given class and generates a {@link MethodHandle method handle} for its getter.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the field is not found.
     *
     * @param clazz        The class to find the method on.
     * @param fieldObfName The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     * @param type         The type of the field to find.
     * @return A handle for the getter of the field with the specified name and type in the given class.
     * @throws UnableToFindFieldException if an issue prevents the field from being reflected
     */
    @API(status = MAINTAINED, since = "1.0.0")
    public static MethodHandle findGetterFromObfName(Class<?> clazz, String fieldObfName, Class<?> type) throws UnableToFindFieldException {
        try {
            String deobfName = getMappingResolver().mapFieldName(INTERMEDIARY, clazz.getName(), fieldObfName, getFieldDescriptor(type));
            return getTrustedLookup(clazz).unreflectGetter(clazz.getDeclaredField(deobfName));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new UnableToFindFieldException(e);
        }
    }

    /**
     * Finds a field with the specified SRG name and type in the given class and generates a {@link MethodHandle method handle} for its setter.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the field is not found.
     *
     * @param clazz        The class to find the method on.
     * @param fieldObfName The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     * @param type         The type of the field to find.
     * @return A handle for the setter of the field with the specified name and type in the given class.
     * @throws UnableToFindFieldException if an issue prevents the field from being reflected
     */
    @API(status = MAINTAINED, since = "1.0.0")
    public static MethodHandle findSetterFromObfName(Class<?> clazz, String fieldObfName, Class<?> type) throws UnableToFindFieldException {
        try {
            String deobfName = getMappingResolver().mapFieldName(INTERMEDIARY, clazz.getName(), fieldObfName, getFieldDescriptor(type));
            return getTrustedLookup(clazz).unreflectSetter(clazz.getDeclaredField(deobfName));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new UnableToFindFieldException(e);
        }
    }

    /**
     * Creates a factory for the given class implementing the given <tt>lambdaType</tt>.
     * The constructor of the class will be looked up using the default public {@link MethodHandles.Lookup} object.
     *
     * @param clazz       the class for which to create a factory
     * @param invokedName the name of the method to implement in the functional interface
     * @param lambdaType  the class of a functional interface that the factory will implement
     * @return a factory implementing <tt>lambdaType</tt>
     * @see #createFactory(Class, String, Class, MethodHandles.Lookup, MethodType, Class[])
     */
    @API(status = MAINTAINED, since = "1.0.0")
    public static <T> T createFactory(Class<?> clazz, String invokedName, Class<? super T> lambdaType) {
        return createFactory(clazz, invokedName, lambdaType, MethodHandles.lookup(), MethodType.methodType(Object.class));
    }

    /**
     * Creates a factory for the given class implementing the given <tt>lambdaType</tt>.
     * The constructor of the class will be looked up using the passed in <tt>lookup</tt> object.
     *
     * @param clazz         Class for which to create a factory
     * @param invokedName   Name of the method to be implemented in the functional interface
     * @param lambdaType    Class of a functional interface to be implemented by the factory
     * @param lookup        Lookup to be used to find the constructor
     * @param samMethodType Signature and return type of method to be implemented by the function object.
     * @return a factory implementing <tt>lambdaType</tt>
     */
    @API(status = MAINTAINED, since = "1.0.0")
    @SuppressWarnings("unchecked")
    public static <T> T createFactory(
            Class<?> clazz,
            String invokedName,
            Class<? super T> lambdaType,
            MethodHandles.Lookup lookup,
            MethodType samMethodType,
            Class<?>... cnstParams
    ) {
        try {
            MethodHandle handle = lookup.findConstructor(clazz, MethodType.methodType(void.class, cnstParams));
            CallSite metafactory = LambdaMetafactory.metafactory(
                    lookup,
                    invokedName,
                    MethodType.methodType(lambdaType),
                    samMethodType,
                    handle,
                    MethodType.methodType(clazz, cnstParams)
            );
            return (T) metafactory.getTarget().invoke();
        } catch (Throwable throwable) {
            throw new UncheckedReflectionException(throwable);
        }
    }

    /**
     * @param clazz the class that the returned lookup should report as its own
     * @return a trusted lookup that has all permissions in the given class
     * @throws UncheckedReflectionException if the black magic does not succeed
     */
    @API(status = MAINTAINED, since = "1.0.0")
    public static MethodHandles.Lookup getTrustedLookup(Class clazz) {
        if (TRUSTED_LOOKUP == null) {
            findTrustedLookup();
        }
        // Invoke black magic.
        return TRUSTED_LOOKUP.in(clazz);
    }

    private static void findTrustedLookup() {
        try {
            // Define black magic.
            // Source: https://gist.github.com/Andrei-Pozolotin/dc8b448dc590183f5459
            final MethodHandles.Lookup original = MethodHandles.lookup();
            final Field internal = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            internal.setAccessible(true);
            TRUSTED_LOOKUP = (MethodHandles.Lookup) internal.get(original);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new UncheckedReflectionException("Could not access trusted lookup", e);
        }
    }

    private static MappingResolver getMappingResolver() {
        return FabricLoader.getInstance().getMappingResolver();
    }
}
