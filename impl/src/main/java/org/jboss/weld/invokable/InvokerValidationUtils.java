package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.logging.InvokerLogger;

/**
 * Utility methods for checking the arguments and instances being used by an {@link Invoker}.
 * <p>
 * Most of these methods return their argument so that they can easily be composed by the invoker builder using
 * {@link MethodHandles#filterArguments(java.lang.invoke.MethodHandle, int, java.lang.invoke.MethodHandle...)}
 * <p>
 * Handles to these methods are obtained via {@link MethodHandleUtils}.
 */
public class InvokerValidationUtils {

    private InvokerValidationUtils() {
    }

    /**
     * Validate that an instance being called by an {@link Invoker} has the expected type or is {@code null}.
     *
     * @param invokerMethod the method being invoked
     * @param type the expected type of the instance
     * @param instance the instance the method is being invoked on
     * @return {@code instance}
     * @throws ClassCastException if the instance is not null and not of the expected type
     */
    static Object instanceHasType(Method invokerMethod, Class<?> type, Object instance) {
        if (instance != null && !type.isInstance(instance)) {
            throw InvokerLogger.LOG.wrongInstanceType(invokerMethod, instance.getClass(), type);
        }
        return instance;
    }

    /**
     * Validate that an instance being called by an {@link Invoker} is not {@code null}.
     *
     * @param invokerMethod the method being invoked
     * @param instance the instance to check
     * @return {@code instance}
     * @throws NullPointerException if {@code instance} is {@code null}
     */
    static Object instanceNotNull(Method invokerMethod, Object instance) {
        if (instance == null) {
            throw InvokerLogger.LOG.nullInstance(invokerMethod);
        }
        return instance;
    }

    /**
     * Validate that an array of arguments for an {@link Invoker} has at least an expected number of elements
     *
     * @param invokerMethod the method being invoked
     * @param requiredArgs the expected number of arguments
     * @param args the array of arguments
     * @return {@code args}
     * @throws IllegalArgumentException if the length of {@code args} is less than {@code requiredArgs}
     */
    static Object[] argCountAtLeast(Method invokerMethod, int requiredArgs, Object[] args) {
        int actualArgs = args == null ? 0 : args.length;
        if (actualArgs < requiredArgs) {
            throw InvokerLogger.LOG.notEnoughArguments(invokerMethod, requiredArgs, actualArgs);
        }
        return args;
    }

    /**
     * Validate that each of the arguments being passed passed to a method by an {@link Invoker} has the correct type.
     * <p>
     * For each pair if type and argument from {@code expectedTypes} and {@code args}:
     * <ul>
     * <li>if the expected type is {@code null}, no validation is done
     * <li>if the expected type is a primitive type, check that the argument is not {@code null} and can be converted to that
     * primitive type using
     * boxing and primitive widening conversions
     * <li>otherwise, check that the argument is an instance of the expected type
     *
     * @param invokerMethod the method being invoked
     * @param expectedTypes an array of the expected type of each argument. May contain {@code null} to indicate that that
     *        argument should not be validated.
     * @param args the array of values being passed as arguments
     * @return {@code args}
     * @throws ClassCastException if any of the arguments are not valid for their expected type
     * @throws NullPointerException if an argument for a primitive-typed parameter is not null
     */
    static Object[] argumentsHaveCorrectType(Method invokerMethod, Class<?>[] expectedTypes, Object[] args) {
        for (int i = 0; i < expectedTypes.length; i++) {
            Class<?> expectedType = expectedTypes[i];
            Object arg = args[i];
            if (expectedType != null) {
                int pos = i + 1; // 1-indexed argument position
                if (expectedType.isPrimitive()) {
                    if (arg == null) {
                        throw InvokerLogger.LOG.nullPrimitiveArgument(invokerMethod, pos);
                    }
                    if (!primitiveConversionPermitted(expectedType, arg.getClass())) {
                        throw InvokerLogger.LOG.wrongArgumentType(invokerMethod, pos, arg.getClass(), expectedType);
                    }
                } else {
                    if (arg != null && !expectedType.isInstance(arg)) {
                        throw InvokerLogger.LOG.wrongArgumentType(invokerMethod, pos, arg.getClass(), expectedType);
                    }
                }
            }
        }
        return args;
    }

    /**
     * Validate whether a reference type can be converted to a primitive type via an unboxing and primitive widening conversion.
     *
     * @param primitive the target primitive type
     * @param actual the reference type to test
     * @return {@code true} if {@code actual} can be converted to {@code primitive} via an unboxing and primitive widening
     *         conversion, otherwise {@code false}
     */
    private static boolean primitiveConversionPermitted(Class<?> primitive, Class<? extends Object> actual) {
        if (primitive == Integer.TYPE) {
            return actual == Integer.class
                    || actual == Character.class
                    || actual == Short.class
                    || actual == Byte.class;
        } else if (primitive == Long.TYPE) {
            return actual == Long.class
                    || actual == Integer.class
                    || actual == Character.class
                    || actual == Short.class
                    || actual == Byte.class;
        } else if (primitive == Boolean.TYPE) {
            return actual == Boolean.class;
        } else if (primitive == Double.TYPE) {
            return actual == Double.class
                    || actual == Float.class
                    || actual == Long.class
                    || actual == Integer.class
                    || actual == Character.class
                    || actual == Short.class
                    || actual == Byte.class;
        } else if (primitive == Float.TYPE) {
            return actual == Float.class
                    || actual == Long.class
                    || actual == Integer.class
                    || actual == Character.class
                    || actual == Short.class
                    || actual == Byte.class;
        } else if (primitive == Short.TYPE) {
            return actual == Short.class
                    || actual == Byte.class;
        } else if (primitive == Character.TYPE) {
            return actual == Character.class;
        } else if (primitive == Byte.TYPE) {
            return actual == Byte.class;
        }
        throw new RuntimeException("Unhandled primitive type: " + primitive);
    }
}
