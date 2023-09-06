package org.jboss.weld.invokable;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.invoke.Invoker;
import jakarta.inject.Named;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class InvokerImpl<T, R> implements Invoker<T, R>, InvokerInfo {

    // method handle for the bean method, including all applied transformers
    private final MethodHandle beanMethodHandle;
    private final MethodHandle invocationWrapper;
    private final Method method;
    // null if no lookup is to be done
    private final Annotation[] instanceLookupQualifiers;
    private final Annotation[][] argLookupQualifiers;
    private final boolean hasInstanceTransformer;
    private final Class<T> beanClass;

    // this variant is only used for invocation wrapper and assumes fully-initialized state
    private InvokerImpl(MethodHandle beanMethodHandle, Annotation[] instanceLookupQualifiers, Annotation[][] argLookupQualifiers,
                        boolean hasInstanceTransformer, Class<T> beanClass, Method method) {
        this.beanMethodHandle = beanMethodHandle;
        this.instanceLookupQualifiers = instanceLookupQualifiers;
        this.argLookupQualifiers = argLookupQualifiers;
        this.hasInstanceTransformer = hasInstanceTransformer;
        this.beanClass = beanClass;
        this.method = method;
        this.invocationWrapper = null;
    }

    InvokerImpl(AbstractInvokerBuilder<T, ?> builder) {
        this.beanClass = builder.beanClass;
        this.method = builder.method;
        if (builder.instanceLookup) {
            this.instanceLookupQualifiers = extractInstanceQualifiers(builder.beanClass, builder.beanManager);
        } else {
            this.instanceLookupQualifiers = null;
        }
        this.argLookupQualifiers = new Annotation[builder.argLookup.length][];
        for (int i = 0; i < builder.argLookup.length; i++) {
            // positions that remain null are those for which we don't perform lookup
            if (builder.argLookup[i]) {
                this.argLookupQualifiers[i] = extractParamQualifiers(builder.method.getParameters()[i], builder.beanManager);
            }
        }
        this.hasInstanceTransformer = builder.instanceTransformer != null;

        // resolve invocation wrapper and save separately
        this.invocationWrapper = builder.invocationWrapper == null ? null : MethodHandleUtils.createMethodHandleFromTransformer(
                builder.method, builder.invocationWrapper, builder.beanClass);

        boolean isStaticMethod = Modifier.isStatic(builder.method.getModifiers());
        int instanceArguments = isStaticMethod ? 0 : 1;

        MethodHandle finalMethodHandle = MethodHandleUtils.createMethodHandle(builder.method);

        // instance transformer
        if (builder.instanceTransformer != null && !isStaticMethod) {
            MethodHandle instanceTransformer = MethodHandleUtils.createMethodHandleFromTransformer(builder.method,
                    builder.instanceTransformer, builder.beanClass);
            if (instanceTransformer.type().parameterCount() == 1) { // no cleanup
                finalMethodHandle = MethodHandles.filterArguments(finalMethodHandle, 0, instanceTransformer);
            } else if (instanceTransformer.type().parameterCount() == 2) { // cleanup
                instanceTransformer = instanceTransformer.asType(instanceTransformer.type().changeParameterType(1, CleanupActions.class));
                finalMethodHandle = MethodHandles.collectArguments(finalMethodHandle, 0, instanceTransformer);
                instanceArguments++;
            } else {
                // internal error, this should not pass validation
                throw new IllegalStateException("Invalid instance transformer method: " + builder.instanceTransformer);
            }
        }

        // argument transformers
        // backwards iteration for correct construction of the resulting parameter list
        for (int i = builder.argTransformers.length - 1; i >= 0; i--) {
            if (builder.argTransformers[i] == null) {
                continue;
            }
            int position = instanceArguments + i;
            MethodHandle argTransformer = MethodHandleUtils.createMethodHandleFromTransformer(builder.method,
                    builder.argTransformers[i], builder.method.getParameterTypes()[i]);
            if (argTransformer.type().parameterCount() == 1) { // no cleanup
                finalMethodHandle = MethodHandles.filterArguments(finalMethodHandle, position, argTransformer);
            } else if (argTransformer.type().parameterCount() == 2) { // cleanup
                argTransformer = argTransformer.asType(argTransformer.type().changeParameterType(1, CleanupActions.class));
                finalMethodHandle = MethodHandles.collectArguments(finalMethodHandle, position, argTransformer);
            } else {
                // internal error, this should not pass validation
                throw new IllegalStateException("Invalid argument transformer method: " + builder.argTransformers[i]);
            }
        }

        // return type transformer
        if (builder.returnValueTransformer != null) {
            MethodHandle returnValueTransformer = MethodHandleUtils.createMethodHandleFromTransformer(builder.method,
                    builder.returnValueTransformer, builder.method.getReturnType());
            finalMethodHandle = MethodHandles.filterReturnValue(finalMethodHandle, returnValueTransformer);
        }

        // exception transformer
        if (builder.exceptionTransformer != null) {
            MethodHandle exceptionTransformer = MethodHandleUtils.createMethodHandleFromTransformer(builder.method,
                    builder.exceptionTransformer, Throwable.class);
            finalMethodHandle = MethodHandles.catchException(finalMethodHandle, Throwable.class, exceptionTransformer);
        }

        // argument reshuffling to support cleanup tasks for input transformers
        //
        // for each input that has a transformer with cleanup, the corresponding argument
        // has a second argument inserted immediately after it, the `CleanupActions` instance;
        // application of the transformer replaces the two arguments with the result
        //
        // inputs without transformations, or with transformations without cleanup, are left
        // intact and application of the transformer only replaces the single argument
        {
            MethodType incomingType = MethodType.methodType(finalMethodHandle.type().returnType(), CleanupActions.class);
            for (Class<?> paramType : finalMethodHandle.type().parameterArray()) {
                if (paramType != CleanupActions.class) {
                    incomingType = incomingType.appendParameterTypes(paramType);
                }
            }
            int[] reordering = new int[finalMethodHandle.type().parameterCount()];
            int paramCounter = 1;
            for (int i = 0; i < reordering.length; i++) {
                if (finalMethodHandle.type().parameterType(i) == CleanupActions.class) {
                    reordering[i] = 0;
                } else {
                    reordering[i] = paramCounter;
                    paramCounter++;
                }
            }
            finalMethodHandle = MethodHandles.permuteArguments(finalMethodHandle, incomingType, reordering);
        }

        // cleanup
        {
            MethodHandle cleanupMethod;
            try {
                String runName = "run"; // to appease a silly checkstyle rule
                cleanupMethod = finalMethodHandle.type().returnType() == void.class
                        ? MethodHandleUtils.createMethodHandle(CleanupActions.class.getMethod(runName, Throwable.class, CleanupActions.class))
                        : MethodHandleUtils.createMethodHandle(CleanupActions.class.getMethod(runName, Throwable.class, Object.class, CleanupActions.class));
            } catch (NoSuchMethodException e) {
                // should never happen
                throw new IllegalStateException("Unable to locate Weld internal helper method");
            }

            if (finalMethodHandle.type().returnType() != void.class) {
                cleanupMethod = cleanupMethod.asType(cleanupMethod.type()
                        .changeReturnType(finalMethodHandle.type().returnType())
                        .changeParameterType(1, finalMethodHandle.type().returnType()));
            }

            finalMethodHandle = MethodHandles.tryFinally(finalMethodHandle, cleanupMethod);
        }

        // spread argument array into individual arguments
        if (isStaticMethod) {
            // keep 1 leading argument, CleanupActions
            MethodHandle invoker = MethodHandles.spreadInvoker(finalMethodHandle.type(), 1);
            invoker = MethodHandles.insertArguments(invoker, 0, finalMethodHandle);
            invoker = MethodHandles.dropArguments(invoker, 1, Object.class);
            finalMethodHandle = invoker;
        } else {
            // keep 2 leading arguments, CleanupActions and the target instance
            MethodHandle invoker = MethodHandles.spreadInvoker(finalMethodHandle.type(), 2);
            invoker = MethodHandles.insertArguments(invoker, 0, finalMethodHandle);
            finalMethodHandle = invoker;
        }

        // assign the bean method handle after applying all transformers
        this.beanMethodHandle = finalMethodHandle;
    }


    @Override
    public R invoke(T instance, Object[] arguments) {
        // if there is an invocation wrapper, just invoke the wrapper immediately
        if (this.invocationWrapper != null) {
            try {
                return (R) invocationWrapper.invoke(instance, arguments, new InvokerImpl<>(beanMethodHandle, instanceLookupQualifiers,
                        argLookupQualifiers, hasInstanceTransformer, beanClass, method));
            } catch (Throwable e) {
                // invocation wrapper or the method itself threw an exception, we just rethrow
                throw new RuntimeException(e);
            }
        }

        CleanupActions cleanup = new CleanupActions();

        // TODO do we want to verify that the T instance is the exact class of the bean?
        if (arguments.length != method.getParameterCount()) {
            // TODO per spec, excess arguments should be dropped
            throw new IllegalArgumentException("Wrong number of args for invoker! Expected: " + method.getParameterCount() + " Provided: " + arguments.length);
        }
        Instance<Object> cdiLookup = CDI.current().getBeanManager().createInstance();
        if (instanceLookupQualifiers != null) {
            // instance lookup set, ignore the parameter we got and lookup the instance
            // standard CDI resolution errors can occur
            instance = getInstance(cleanup, cdiLookup, beanClass, instanceLookupQualifiers);
        } else {
            // arg should not be null unless the method is (a) static, (b) has instance transformer, (c) has instance lookup
            if (instance == null && !Modifier.isStatic(method.getModifiers()) && !hasInstanceTransformer) {
                // TODO better exception
                throw new IllegalArgumentException("Invoker target instance cannot be null unless it's static method or set for CDI lookup!");
            }
        }

        for (int i = 0; i < argLookupQualifiers.length; i++) {
            if (argLookupQualifiers[i] != null) {
                // TODO what if parameter type is generic?
                arguments[i] = getInstance(cleanup, cdiLookup, method.getParameterTypes()[i], argLookupQualifiers[i]);
            }
        }

        try {
            return (R) beanMethodHandle.invoke(cleanup, instance, arguments);
        } catch (ValueCarryingException e) {
            // exception transformer may return a value by throwing a special exception
            return (R) e.getMethodReturnValue();
        } catch (Throwable e) {
            // TODO at this point there might have been exception transformer invoked as well, guess we just rethrow?
            // we just rethrow the original exception
            throw new RuntimeException(e);
        }
    }

    private <TYPE> TYPE getInstance(CleanupActions cleanup, Instance<Object> lookup, Class<TYPE> classToLookup, Annotation... qualifiers) {
        Instance.Handle<TYPE> handle = lookup.select(classToLookup, qualifiers).getHandle();
        cleanup.addInstanceHandle(handle);
        return handle.get();
    }

    private Annotation[] extractInstanceQualifiers(Class<?> beanClass, BeanManager bm) {
        return extractQualifiers(beanClass.getAnnotations(), bm);
    }

    private Annotation[] extractParamQualifiers(Parameter parameter, BeanManager bm) {
        return extractQualifiers(parameter.getAnnotations(), bm);
    }

    private Annotation[] extractQualifiers(Annotation[] annotations, BeanManager bm) {
        List<Annotation> qualifiers = new ArrayList<>();
        for (Annotation a : annotations) {
            if (bm.isQualifier(a.annotationType())) {
                qualifiers.add(a);
            }
        }
        // add default when there are no qualifiers or just @Named
        if (qualifiers.isEmpty() || (qualifiers.size() == 1 && qualifiers.get(0).annotationType().equals(Named.class))) {
            qualifiers.add(Default.Literal.INSTANCE);
        }
        return qualifiers.toArray(new Annotation[]{});
    }

}
