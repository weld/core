package org.jboss.weld.invokable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.exceptions.DeploymentException;

class MethodHandleUtils {
    private MethodHandleUtils() {
    }

    static final MethodHandle CLEANUP_ACTIONS_CTOR;
    static final MethodHandle CLEANUP_FOR_VOID;
    static final MethodHandle CLEANUP_FOR_NONVOID;
    static final MethodHandle LOOKUP;
    static final MethodHandle REPLACE_PRIMITIVE_LOOKUP_NULLS;
    static final MethodHandle THROW_VALUE_CARRYING_EXCEPTION;
    static final MethodHandle TRIM_ARRAY_TO_SIZE;

    static {
        try {
            CLEANUP_ACTIONS_CTOR = createMethodHandle(CleanupActions.class.getDeclaredConstructor());
            String runName = "run";
            CLEANUP_FOR_VOID = createMethodHandle(CleanupActions.class.getMethod(
                    runName, Throwable.class, CleanupActions.class));
            CLEANUP_FOR_NONVOID = createMethodHandle(CleanupActions.class.getMethod(
                    runName, Throwable.class, Object.class, CleanupActions.class));
            LOOKUP = createMethodHandle(LookupUtils.class.getDeclaredMethod(
                    "lookup", CleanupActions.class, BeanManager.class, Type.class, Annotation[].class));
            REPLACE_PRIMITIVE_LOOKUP_NULLS = MethodHandleUtils.createMethodHandle(LookupUtils.class.getDeclaredMethod(
                    "replacePrimitiveLookupNulls", Object[].class, Class[].class, boolean[].class));
            THROW_VALUE_CARRYING_EXCEPTION = createMethodHandle(ValueCarryingException.class.getDeclaredMethod(
                    "throwReturnValue", Object.class));
            TRIM_ARRAY_TO_SIZE = createMethodHandle(ArrayUtils.class.getDeclaredMethod(
                    "trimArrayToSize", Object[].class, int.class));
        } catch (NoSuchMethodException e) {
            // should never happen
            throw new IllegalStateException("Unable to locate Weld internal helper method", e);
        }
    }

    private static MethodHandles.Lookup lookupFor(Executable method) throws IllegalAccessException {
        if (Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
            return MethodHandles.publicLookup();
        }

        // to create a method handle for a `protected`, package-private or `private` method,
        // we need a private lookup in the declaring class
        Module thisModule = MethodHandleUtils.class.getModule();
        Class<?> targetClass = method.getDeclaringClass();
        Module targetModule = targetClass.getModule();
        if (!thisModule.canRead(targetModule)) {
            // we need to read the other module in order to have privateLookup access
            // see javadoc for MethodHandles.privateLookupIn()
            thisModule.addReads(targetModule);
        }
        return MethodHandles.privateLookupIn(targetClass, MethodHandles.lookup());
    }

    static MethodHandle createMethodHandle(Method method) {
        try {
            return lookupFor(method).unreflect(method);
        } catch (ReflectiveOperationException e) {
            // TODO proper exception handling
            throw new RuntimeException(e);
        }
    }

    static MethodHandle createMethodHandle(Constructor<?> constructor) {
        try {
            return lookupFor(constructor).unreflectConstructor(constructor);
        } catch (ReflectiveOperationException e) {
            // TODO proper exception handling
            throw new RuntimeException(e);
        }
    }

    static MethodHandle createMethodHandleFromTransformer(Method targetMethod, TransformerMetadata transformer,
            Class<?> transformationArgType) {
        List<Method> matchingMethods = new ArrayList<>();
        // transformers must be `public` and may be inherited (if not `static`)
        for (Method m : transformer.getDeclaringClass().getMethods()) {
            // `static` transformers must be declared directly on the given class,
            // instance transformers may be inherited
            if (Modifier.isStatic(m.getModifiers()) && !m.getDeclaringClass().equals(transformer.getDeclaringClass())) {
                continue;
            }
            // method match is only based on class and name, no match is a problem and so are multiple
            if (m.getName().equals(transformer.getMethodName())) {
                matchingMethods.add(m);
            }
        }
        if (matchingMethods.isEmpty()) {
            // TODO better exception, use Logger interface
            throw new DeploymentException(transformer + ": no method found");
        }
        if (matchingMethods.size() > 1) {
            // TODO better exception, use Logger interface
            throw new DeploymentException(transformer + ": more than one method found: " + matchingMethods);
        }
        Method method = matchingMethods.get(0);

        // validate method
        validateTransformerMethod(method, transformer, transformationArgType);

        MethodHandle result = createMethodHandle(method);

        // for input transformers, we might need to change return type to whatever the original method expects
        // for output transformers, we might need to change their input params
        // this enables transformers to operate on subclasses (input tf) or superclasses (output tf)
        if (transformer.isInputTransformer()
                && !result.type().returnType().equals(transformationArgType)) {
            result = result.asType(result.type().changeReturnType(transformationArgType));
        } else if (transformer.isOutputTransformer()
                && result.type().parameterCount() > 0
                && !result.type().parameterType(0).equals(transformationArgType)) {
            result = result.asType(result.type().changeParameterType(0, transformationArgType));
        }
        if (TransformerType.EXCEPTION.equals(transformer.getType())) {
            // if assignable, then just alter return type
            if (targetMethod.getReturnType().isAssignableFrom(result.type().returnType())) {
                // exception handlers can return a subtype of original class
                // so long as it is assignable, just cast it to the required/expected type
                result = result.asType(result.type().changeReturnType(targetMethod.getReturnType()));
            } else {
                // if not assignable, then we need to apply a return value transformer which hides the value in an exception
                MethodHandle throwReturnValue = THROW_VALUE_CARRYING_EXCEPTION;
                // cast return value of the custom method we use to whatever the original method expects - we'll never use it anyway
                throwReturnValue = throwReturnValue
                        .asType(throwReturnValue.type().changeReturnType(targetMethod.getReturnType()));
                // adapt the parameter type as well, we don't really care what it is, we just store it and throw it
                throwReturnValue = throwReturnValue
                        .asType(throwReturnValue.type().changeParameterType(0, result.type().returnType()));
                result = MethodHandles.filterReturnValue(result, throwReturnValue);
            }
        }
        return result;
    }

    private static void validateTransformerMethod(Method m, TransformerMetadata transformer, Class<?> transformationArgType) {
        // all transformers have to be public to ensure accessibility
        if (!Modifier.isPublic(m.getModifiers())) {
            // TODO better exception, use Logger interface
            throw new DeploymentException("All invocation transformers need to be public - " + transformer);
        }

        int paramCount = m.getParameterCount();
        if (transformer.isInputTransformer()) {
            // input transformers need to validate assignability of their return type versus original arg type
            if (!transformationArgType.isAssignableFrom(m.getReturnType())) {
                // TODO better exception, use Logger interface
                throw new DeploymentException("Input transformer " + transformer
                        + " has a return value that is not assignable to expected class: " + transformationArgType);
            }
            // instance method is no-param, otherwise its 1-2 with second being Consumer<Runnable>
            if (!Modifier.isStatic(m.getModifiers())) {
                if (paramCount != 0) {
                    // TODO better exception, use Logger interface
                    throw new DeploymentException(
                            "Non-static input transformers are expected to have zero input parameters! Transformer: "
                                    + transformer);
                }
            } else {
                if (paramCount > 2) {
                    // TODO better exception, use Logger interface
                    throw new DeploymentException(
                            "Static input transformers can only have one or two parameters. " + transformer);
                }
                if (paramCount == 2) {
                    // we do not validate type param of Consumer, i.e. if it's exactly Consumer<Runnable>
                    if (!Consumer.class.equals(m.getParameters()[1].getType())) {
                        // TODO better exception, use Logger interface
                        throw new DeploymentException(
                                "Static input transformers with two parameters can only have Consumer<Runnable> as their second parameter! "
                                        + transformer);
                    }
                }
            }
        } else if (transformer.isOutputTransformer()) {
            // output transformers need to validate assignability of their INPUT
            // this also means instance methods need no validation in this regard
            if (!Modifier.isStatic(m.getModifiers())) {
                if (paramCount != 0) {
                    // TODO better exception, use Logger interface
                    throw new DeploymentException(
                            "Non-static output transformers are expected to have zero input parameters! Transformer: "
                                    + transformer);
                }
            } else {
                if (paramCount != 1) {
                    // TODO better exception, use Logger interface
                    throw new DeploymentException(
                            "Static output transformers are expected to have one input parameter! Transformer: " + transformer);
                }
                if (!m.getParameters()[0].getType().isAssignableFrom(transformationArgType)) {
                    // TODO better exception, use Logger interface
                    throw new DeploymentException("Output transformer " + transformer
                            + " parameter is not assignable to the expected type " + transformationArgType);
                }
            }
        } else {
            // wrapper has exactly three arguments
            Class<?>[] params = m.getParameterTypes();
            if (params.length == 3
                    && params[0].equals(transformationArgType)
                    && params[1].equals(Object[].class)
                    && params[2].equals(Invoker.class)) {
                // OK
            } else {
                // TODO better exception, use Logger interface
                throw new DeploymentException("Invocation wrapper has unexpected parameters " + transformer
                        + "\nExpected param types are: " + transformationArgType + ", Object[], Invoker.class");
            }
        }
    }
}
