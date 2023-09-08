package org.jboss.weld.invokable;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.invoke.InvokerBuilder;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

// TODO deployment-time validation of configured lookups
abstract class AbstractInvokerBuilder<B, T> implements InvokerBuilder<T> {

    final Class<B> beanClass;
    // work with reflection representation so that we can re-use this logic from within BCE
    final Method method;

    boolean instanceLookup;
    boolean[] argLookup;
    TransformerMetadata instanceTransformer;
    TransformerMetadata[] argTransformers;
    TransformerMetadata returnValueTransformer;
    TransformerMetadata exceptionTransformer;
    TransformerMetadata invocationWrapper;

    final BeanManager beanManager;

    public AbstractInvokerBuilder(Class<B> beanClass, Method method, BeanManager beanManager) {
        this.beanClass = beanClass;
        this.method = method;
        this.argLookup = new boolean[method.getParameterCount()];
        this.argTransformers = new TransformerMetadata[method.getParameterCount()];
        this.beanManager = beanManager;
    }

    @Override
    public InvokerBuilder<T> setInstanceLookup() {
        this.instanceLookup = true;
        return this;
    }

    @Override
    public InvokerBuilder<T> setArgumentLookup(int position) {
        if (position >= argLookup.length) {
            // TODO better exception
            throw new IllegalArgumentException("Error attempting to set CDI argument lookup for arg number " + position + " while the number of method args is " + argLookup.length);
        }
        argLookup[position] = true;
        return this;
    }

    @Override
    public InvokerBuilder<T> setInstanceTransformer(Class<?> clazz, String methodName) {
        if (instanceTransformer != null) {
            // TODO better exception
            throw new IllegalStateException("Instance transformer already set");
        }
        this.instanceTransformer = new TransformerMetadata(clazz, methodName, TransformerType.INSTANCE);
        return this;
    }

    @Override
    public InvokerBuilder<T> setArgumentTransformer(int position, Class<?> clazz, String methodName) {
        if (position >= argTransformers.length) {
            // TODO better exception
            throw new IllegalArgumentException("Error attempting to set an argument lookup. Number of method args: " + argLookup.length + " arg position: " + position);
        }
        if (argTransformers[position] != null) {
            // TODO better exception
            throw new IllegalStateException("Argument transformer " + position + " already set");
        }
        this.argTransformers[position] = new TransformerMetadata(clazz, methodName, TransformerType.ARGUMENT);
        return this;
    }

    @Override
    public InvokerBuilder<T> setReturnValueTransformer(Class<?> clazz, String methodName) {
        if (returnValueTransformer != null) {
            // TODO better exception
            throw new IllegalStateException("Return value transformer already set");
        }
        this.returnValueTransformer = new TransformerMetadata(clazz, methodName, TransformerType.RETURN_VALUE);
        return this;
    }

    @Override
    public InvokerBuilder<T> setExceptionTransformer(Class<?> clazz, String methodName) {
        if (exceptionTransformer != null) {
            // TODO better exception
            throw new IllegalStateException("Exception transformer already set");
        }
        this.exceptionTransformer = new TransformerMetadata(clazz, methodName, TransformerType.EXCEPTION);
        return this;
    }

    @Override
    public InvokerBuilder<T> setInvocationWrapper(Class<?> clazz, String methodName) {
        if (invocationWrapper != null) {
            // TODO better exception
            throw new IllegalStateException("Invocation wrapper already set");
        }
        this.invocationWrapper = new TransformerMetadata(clazz, methodName, TransformerType.WRAPPER);
        return this;
    }

    InvokerImpl<B, ?> doBuild() {
        boolean isStaticMethod = Modifier.isStatic(method.getModifiers());
        int instanceArguments = isStaticMethod ? 0 : 1;

        MethodHandle mh = MethodHandleUtils.createMethodHandle(method);

        // instance transformer
        if (instanceTransformer != null && !isStaticMethod) {
            MethodHandle instanceTransformerMethod = MethodHandleUtils.createMethodHandleFromTransformer(method,
                    instanceTransformer, beanClass);
            if (instanceTransformerMethod.type().parameterCount() == 1) { // no cleanup
                mh = MethodHandles.filterArguments(mh, 0, instanceTransformerMethod);
            } else if (instanceTransformerMethod.type().parameterCount() == 2) { // cleanup
                instanceTransformerMethod = instanceTransformerMethod.asType(instanceTransformerMethod.type().changeParameterType(1, CleanupActions.class));
                mh = MethodHandles.collectArguments(mh, 0, instanceTransformerMethod);
                instanceArguments++;
            } else {
                // internal error, this should not pass validation
                throw new IllegalStateException("Invalid instance transformer method: " + instanceTransformer);
            }
        }

        // argument transformers
        // backwards iteration for correct construction of the resulting parameter list
        for (int i = argTransformers.length - 1; i >= 0; i--) {
            if (argTransformers[i] == null) {
                continue;
            }
            int position = instanceArguments + i;
            MethodHandle argTransformerMethod = MethodHandleUtils.createMethodHandleFromTransformer(method,
                    argTransformers[i], method.getParameterTypes()[i]);
            if (argTransformerMethod.type().parameterCount() == 1) { // no cleanup
                mh = MethodHandles.filterArguments(mh, position, argTransformerMethod);
            } else if (argTransformerMethod.type().parameterCount() == 2) { // cleanup
                argTransformerMethod = argTransformerMethod.asType(argTransformerMethod.type().changeParameterType(1, CleanupActions.class));
                mh = MethodHandles.collectArguments(mh, position, argTransformerMethod);
            } else {
                // internal error, this should not pass validation
                throw new IllegalStateException("Invalid argument transformer method: " + argTransformers[i]);
            }
        }

        // return type transformer
        if (returnValueTransformer != null) {
            MethodHandle returnValueTransformerMethod = MethodHandleUtils.createMethodHandleFromTransformer(method,
                    returnValueTransformer, method.getReturnType());
            mh = MethodHandles.filterReturnValue(mh, returnValueTransformerMethod);
        }

        // exception transformer
        if (exceptionTransformer != null) {
            MethodHandle exceptionTransformerMethod = MethodHandleUtils.createMethodHandleFromTransformer(method,
                    exceptionTransformer, Throwable.class);
            mh = MethodHandles.catchException(mh, Throwable.class, exceptionTransformerMethod);
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
            MethodType incomingType = MethodType.methodType(mh.type().returnType(), CleanupActions.class);
            for (Class<?> paramType : mh.type().parameterArray()) {
                if (paramType != CleanupActions.class) {
                    incomingType = incomingType.appendParameterTypes(paramType);
                }
            }
            int[] reordering = new int[mh.type().parameterCount()];
            int paramCounter = 1;
            for (int i = 0; i < reordering.length; i++) {
                if (mh.type().parameterType(i) == CleanupActions.class) {
                    reordering[i] = 0;
                } else {
                    reordering[i] = paramCounter;
                    paramCounter++;
                }
            }
            mh = MethodHandles.permuteArguments(mh, incomingType, reordering);
        }

        MethodHandle lookupMethod;
        try {
            lookupMethod = MethodHandleUtils.createMethodHandle(LookupUtils.class.getDeclaredMethod("lookup",
                    CleanupActions.class, BeanManager.class, Type.class, Annotation[].class));
        } catch (NoSuchMethodException e) {
            // should never happen
            throw unableToLocateWeldInternalHelperMethod();
        }

        MethodType typeBeforeLookups = mh.type();
        int positionsBeforeArguments = 1; // first `CleanupActions` we need to preserve for transformations
        if (!isStaticMethod) {
            positionsBeforeArguments++; // the target instance
        }

        // instance lookup
        if (instanceLookup && !isStaticMethod) {
            Type type = beanClass;
            Class<?> parameterType = typeBeforeLookups.parameterType(1);
            Annotation[] qualifiers = LookupUtils.classQualifiers(beanClass, beanManager);
            MethodHandle instanceLookupMethod = lookupMethod;
            instanceLookupMethod = MethodHandles.insertArguments(instanceLookupMethod, 1, beanManager, type, qualifiers);
            instanceLookupMethod = instanceLookupMethod.asType(instanceLookupMethod.type().changeReturnType(parameterType));
            instanceLookupMethod = MethodHandles.dropArguments(instanceLookupMethod, 0, parameterType);
            mh = MethodHandles.collectArguments(mh, 1, instanceLookupMethod);
            positionsBeforeArguments++; // second `CleanupActions`
        }

        // arguments lookup
        // backwards iteration for correct construction of the resulting parameter list
        for (int i = argLookup.length - 1; i >= 0; i--) {
            if (!argLookup[i]) {
                continue;
            }
            int position = positionsBeforeArguments + i;
            Parameter parameter = method.getParameters()[i];
            Type type = parameter.getParameterizedType();
            Class<?> parameterType = typeBeforeLookups.parameterType(i + (isStaticMethod ? 1 : 2));
            Annotation[] qualifiers = LookupUtils.parameterQualifiers(parameter, beanManager);
            MethodHandle argumentLookupMethod = lookupMethod;
            argumentLookupMethod = MethodHandles.insertArguments(argumentLookupMethod, 1, beanManager, type, qualifiers);
            argumentLookupMethod = argumentLookupMethod.asType(argumentLookupMethod.type().changeReturnType(parameterType));
            argumentLookupMethod = MethodHandles.dropArguments(argumentLookupMethod, 0, parameterType);
            mh = MethodHandles.collectArguments(mh, position, argumentLookupMethod);
        }

        // argument reshuffling to support cleanup tasks for input lookups
        //
        // for each input that has a lookup, the corresponding argument
        // has a second argument inserted immediately after it, the `CleanupActions` instance;
        // application of the lookup replaces the two arguments with the result
        //
        // inputs without lookup are left intact and application of the transformer
        // only replaces the single argument
        {
            int[] reordering = new int[mh.type().parameterCount()];
            int paramCounter = 1;
            for (int i = 0; i < reordering.length; i++) {
                if (mh.type().parameterType(i) == CleanupActions.class) {
                    reordering[i] = 0;
                } else {
                    reordering[i] = paramCounter;
                    paramCounter++;
                }
            }
            mh = MethodHandles.permuteArguments(mh, typeBeforeLookups, reordering);
        }

        // cleanup
        {
            MethodHandle cleanupMethod;
            try {
                String runName = "run"; // to appease a silly checkstyle rule
                cleanupMethod = mh.type().returnType() == void.class
                        ? MethodHandleUtils.createMethodHandle(CleanupActions.class.getMethod(runName, Throwable.class, CleanupActions.class))
                        : MethodHandleUtils.createMethodHandle(CleanupActions.class.getMethod(runName, Throwable.class, Object.class, CleanupActions.class));
            } catch (NoSuchMethodException e) {
                // should never happen
                throw unableToLocateWeldInternalHelperMethod();
            }

            if (mh.type().returnType() != void.class) {
                cleanupMethod = cleanupMethod.asType(cleanupMethod.type()
                        .changeReturnType(mh.type().returnType())
                        .changeParameterType(1, mh.type().returnType()));
            }

            mh = MethodHandles.tryFinally(mh, cleanupMethod);
        }

        // spread argument array into individual arguments
        if (isStaticMethod) {
            // keep 1 leading argument, CleanupActions
            MethodHandle invoker = MethodHandles.spreadInvoker(mh.type(), 1);
            invoker = MethodHandles.insertArguments(invoker, 0, mh);
            invoker = MethodHandles.dropArguments(invoker, 1, Object.class);
            mh = invoker;
        } else {
            // keep 2 leading arguments, CleanupActions and the target instance
            MethodHandle invoker = MethodHandles.spreadInvoker(mh.type(), 2);
            invoker = MethodHandles.insertArguments(invoker, 0, mh);
            mh = invoker;
        }

        // replace `null` values in the arguments array with zero values
        // on positions where the method accepts a primitive type
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (PrimitiveUtils.hasPrimitive(parameterTypes)) {
            MethodHandle replacePrimitiveNulls = null;
            try {
                replacePrimitiveNulls = MethodHandleUtils.createMethodHandle(PrimitiveUtils.class.getDeclaredMethod(
                        "replacePrimitiveNulls", Object[].class, Class[].class));
            } catch (NoSuchMethodException e) {
                // should never happen
                throw unableToLocateWeldInternalHelperMethod();
            }
            replacePrimitiveNulls = MethodHandles.insertArguments(replacePrimitiveNulls, 1, (Object) parameterTypes);
            mh = MethodHandles.filterArguments(mh, 2, replacePrimitiveNulls);
        }

        // instantiate `CleanupActions`
        {
            MethodHandle cleanupActions;
            try {
                cleanupActions = MethodHandleUtils.createMethodHandle(CleanupActions.class.getDeclaredConstructor());
            } catch (NoSuchMethodException e) {
                // should never happen
                throw unableToLocateWeldInternalHelperMethod();
            }
            mh = MethodHandles.foldArguments(mh, cleanupActions);
        }

        // create an inner invoker and pass it to wrapper
        if (invocationWrapper != null) {
            InvokerImpl<?, ?> invoker = new InvokerImpl<>(mh);

            MethodHandle invocationWrapperMethod = MethodHandleUtils.createMethodHandleFromTransformer(method,
                    invocationWrapper, beanClass);

            mh = MethodHandles.insertArguments(invocationWrapperMethod, 2, invoker);
        }

        return new InvokerImpl<>(mh);
    }

    private static IllegalStateException unableToLocateWeldInternalHelperMethod() {
        return new IllegalStateException("Unable to locate Weld internal helper method");
    }
}
