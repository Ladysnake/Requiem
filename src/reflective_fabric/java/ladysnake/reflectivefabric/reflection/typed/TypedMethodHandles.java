package ladysnake.reflectivefabric.reflection.typed;

import ladysnake.reflectivefabric.reflection.UnableToFindFieldException;
import ladysnake.reflectivefabric.reflection.UnableToFindMethodException;
import org.apiguardian.api.API;

import static ladysnake.reflectivefabric.reflection.ReflectionHelper.*;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * This class consists exclusively of static methods that operate on or return
 * {@link TypedMethod typed method handles}. They fall into two categories:
 * <ul>
 * <li>Lookup methods which help create typed method handles for methods and fields.
 * <li>Combinator methods, which combine or transform pre-existing method handles into new ones.
 * </ul>
 * <p>
 * @author Pyrofab
 * @since 2.6
 */
public final class TypedMethodHandles {
    private TypedMethodHandles() { throw new AssertionError(); }

    /**
     * Finds a field with the specified SRG name and type in the given class and generates a {@link TypedSetter} for it.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the field is not found.
     *
     * @param clazz        The class to find the method on.
     * @param fieldObfName The SRG name of the field to find (e.g. <tt>"field_71100_bB"</tt>).
     * @param type         The type of the field to find.
     * @return A handle for the setter of the field with the specified name and type in the given class.
     * @throws UnableToFindFieldException if an issue prevents the field from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public static <T, R> TypedGetter<T, R> findGetter(Class<T> clazz, String fieldObfName, Class<? super R> type) {
        return new TypedGetter<>(findGetterFromObfName(clazz, fieldObfName, type), fieldObfName);
    }

    /**
     * Finds a field with the specified SRG name and type in the given class and generates a {@link TypedSetter} for it.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the field is not found.
     *
     * @param clazz        The class to find the method on.
     * @param fieldObfName The SRG name of the field to find (e.g. <tt>"field_71100_bB"</tt>).
     * @param type         The type of the field to find.
     * @return A handle for the getter of the field with the specified name and type in the given class.
     * @throws UnableToFindFieldException if an issue prevents the field from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public static <T, R> TypedSetter<T, R> findSetter(Class<T> clazz, String fieldObfName, Class<? super R> type) {
        return new TypedSetter<>(findSetterFromObfName(clazz, fieldObfName, type), fieldObfName);
    }

    /**
     * Finds a field with the specified SRG name and type in the given class and generates a {@link RWTypedField}
     * combining its getter and setter.
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the field is not found.
     *
     * @param clazz        The class to find the method on.
     * @param fieldObfName The SRG name of the field to find (e.g. <tt>"field_71100_bB"</tt>).
     * @param type         The type of the field to find.
     * @return A handle for the getter of the field with the specified name and type in the given class.
     * @throws UnableToFindFieldException if an issue prevents the field from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public static <T, R> RWTypedField<T, R> createFieldRef(Class<T> clazz, String fieldObfName, Class<? super R> type) {
        TypedGetter<T, R> getter = findGetter(clazz, fieldObfName, type);
        TypedSetter<T, R> setter = findSetter(clazz, fieldObfName, type);
        return new RWTypedField<>(getter, setter);
    }

    /**
     * Finds a no-args method with the specified SRG name in the given class and generates a {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public static <T, R> TypedMethod0<T, R> findVirtual(Class<T> clazz, String methodObfName, Class<? super R> returnType) {
        return new TypedMethod0<>(findMethodHandleFromObfName(clazz, methodObfName, returnType), methodObfName);
    }

    /**
     * Finds a no-args static method with the specified SRG name in the given class and generates a {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 3.0.0
     */
    @API(status = EXPERIMENTAL, since = "3.0.0")
    public static <T, R> TypedStaticMethod0<R> findStatic(Class<T> clazz, String methodObfName, Class<? super R> returnType) {
        return new TypedStaticMethod0<>(findMethodHandleFromObfName(clazz, methodObfName, returnType), methodObfName, clazz);
    }

    /**
     * Finds a method taking one argument, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public static <T, P1, R> TypedMethod1<T, P1, R> findVirtual(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1) {
        return new TypedMethod1<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1), methodObfName);
    }

    /**
     * Finds a static method taking one argument, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 3.0.0
     */
    @API(status = EXPERIMENTAL, since = "3.0.0")
    public static <T, P1, R> TypedStaticMethod1<P1, R> findStatic(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1) {
        return new TypedStaticMethod1<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1), methodObfName, clazz);
    }

    /**
     * Finds a method taking two arguments, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @param p2             The declared type of the second parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public static <T, P1, P2, R> TypedMethod2<T, P1, P2, R> findVirtual(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1, Class<? super P2> p2) {
        return new TypedMethod2<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1, p2), methodObfName);
    }

    /**
     * Finds a static method taking two arguments, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @param p2             The declared type of the second parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "3.0.0")
    public static <T, P1, P2, R> TypedStaticMethod2<P1, P2, R> findStatic(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1, Class<? super P2> p2) {
        return new TypedStaticMethod2<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1, p2), methodObfName, clazz);
    }

    /**
     * Finds a method taking three arguments, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @param p2             The declared type of the second parameter of the method
     * @param p3             The declared type of the third parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public static <T, P1, P2, P3, R> TypedMethod3<T, P1, P2, P3, R> findVirtual(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1, Class<? super P2> p2, Class<? super P3> p3) {
        return new TypedMethod3<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1, p2, p3), methodObfName);
    }

    /**
     * Finds a static method taking three arguments, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @param p2             The declared type of the second parameter of the method
     * @param p3             The declared type of the third parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "3.0.0")
    public static <T, P1, P2, P3, R> TypedStaticMethod3<P1, P2, P3, R> findStatic(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1, Class<? super P2> p2, Class<? super P3> p3) {
        return new TypedStaticMethod3<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1, p2, p3), methodObfName, clazz);
    }

    /**
     * Finds a method taking four arguments, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @param p2             The declared type of the second parameter of the method
     * @param p3             The declared type of the third parameter of the method
     * @param p4             The declared type of the fourth parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public static <T, P1, P2, P3, P4, R> TypedMethod4<T, P1, P2, P3, P4, R> findVirtual(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1, Class<? super P2> p2, Class<? super P3> p3, Class<? super P4> p4) {
        return new TypedMethod4<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1, p2, p3, p4), methodObfName);
    }

    /**
     * Finds a static method taking four arguments, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @param p2             The declared type of the second parameter of the method
     * @param p3             The declared type of the third parameter of the method
     * @param p4             The declared type of the fourth parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "3.0.0")
    public static <T, P1, P2, P3, P4, R> TypedStaticMethod4<P1, P2, P3, P4, R> findStatic(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1, Class<? super P2> p2, Class<? super P3> p3, Class<? super P4> p4) {
        return new TypedStaticMethod4<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1, p2, p3, p4), methodObfName, clazz);
    }

    /**
     * Finds a method taking five arguments, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @param p2             The declared type of the second parameter of the method
     * @param p3             The declared type of the third parameter of the method
     * @param p4             The declared type of the fourth parameter of the method
     * @param p5             The declared type of the fifth parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public static <T, P1, P2, P3, P4, P5, R> TypedMethod5<T, P1, P2, P3, P4, P5, R> findVirtual(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1, Class<? super P2> p2, Class<? super P3> p3, Class<? super P4> p4, Class<? super P5> p5) {
        return new TypedMethod5<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1, p2, p3, p4), methodObfName);
    }

    /**
     * Finds a method taking five arguments, with the specified SRG name, in the given class and generates a
     * {@link TypedMethod typed method handle} for it. <br>
     * Note: for performance, store the returned value and avoid calling this repeatedly.
     * <p>
     * Throws an exception if the method is not found.
     *
     * @param clazz          The class to find the method on.
     * @param methodObfName  The obfuscated name of the method to find (used in obfuscated environments, i.e. "getWorldTime").
     *                       If the name you are looking for is on a class that is never obfuscated, this should be null.
     * @param returnType     The return type of the method to find.
     * @param p1             The declared type of the first parameter of the method
     * @param p2             The declared type of the second parameter of the method
     * @param p3             The declared type of the third parameter of the method
     * @param p4             The declared type of the fourth parameter of the method
     * @param p5             The declared type of the fifth parameter of the method
     * @return A handle for the method with the specified name and parameters in the given class.
     * @throws UnableToFindMethodException if an issue prevents the method from being reflected
     * @since 2.6.0
     */
    @API(status = EXPERIMENTAL, since = "3.0.0")
    public static <T, P1, P2, P3, P4, P5, R> TypedStaticMethod5<P1, P2, P3, P4, P5, R> findStatic(Class<T> clazz, String methodObfName, Class<? super R> returnType, Class<? super P1> p1, Class<? super P2> p2, Class<? super P3> p3, Class<? super P4> p4, Class<? super P5> p5) {
        return new TypedStaticMethod5<>(findMethodHandleFromObfName(clazz, methodObfName, returnType, p1, p2, p3, p4), methodObfName, clazz);
    }

}
