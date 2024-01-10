package org.jboss.weld.invokable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.weld.invoke.WeldInvokerBuilder;
import org.jboss.weld.manager.BeanManagerImpl;

public abstract class AbstractInvokerBuilder<B, T> implements WeldInvokerBuilder<T> {
    final AnnotatedType<B> beanClass;
    final TargetMethod method;

    boolean instanceLookup;
    boolean[] argLookup;
    TransformerMetadata instanceTransformer;
    TransformerMetadata[] argTransformers;
    TransformerMetadata returnValueTransformer;
    TransformerMetadata exceptionTransformer;
    TransformerMetadata invocationWrapper;

    final BeanManager beanManager;

    AbstractInvokerBuilder(AnnotatedType<B> beanClass, TargetMethod method, BeanManagerImpl beanManager) {
        this.beanClass = beanClass;
        this.method = method;
        this.argLookup = new boolean[method.getParameterCount()];
        this.argTransformers = new TransformerMetadata[method.getParameterCount()];
        this.beanManager = beanManager;
        beanManager.addInvoker(this);
    }

    @Override
    public WeldInvokerBuilder<T> withInstanceLookup() {
        this.instanceLookup = true;
        return this;
    }

    @Override
    public WeldInvokerBuilder<T> withArgumentLookup(int position) {
        if (position < 0 || position >= argLookup.length) {
            // TODO better exception, use Logger interface
            throw new IllegalArgumentException("Cannot lookup argument " + position
                    + ", the number of method parameters is " + argLookup.length);
        }
        argLookup[position] = true;
        return this;
    }

    @Override
    public WeldInvokerBuilder<T> withInstanceTransformer(Class<?> clazz, String methodName) {
        if (instanceTransformer != null) {
            // TODO better exception, use Logger interface
            throw new IllegalStateException("Instance transformer already set");
        }
        this.instanceTransformer = new TransformerMetadata(clazz, methodName, TransformerType.INSTANCE);
        return this;
    }

    @Override
    public WeldInvokerBuilder<T> withArgumentTransformer(int position, Class<?> clazz, String methodName) {
        if (position < 0 || position >= argTransformers.length) {
            // TODO better exception, use Logger interface
            throw new IllegalArgumentException("Cannot transform argument " + position
                    + ", the number of method parameters is " + argLookup.length);
        }
        if (argTransformers[position] != null) {
            // TODO better exception, use Logger interface
            throw new IllegalStateException("Argument transformer " + position + " already set");
        }
        this.argTransformers[position] = new TransformerMetadata(clazz, methodName, TransformerType.ARGUMENT);
        return this;
    }

    @Override
    public WeldInvokerBuilder<T> withReturnValueTransformer(Class<?> clazz, String methodName) {
        if (returnValueTransformer != null) {
            // TODO better exception, use Logger interface
            throw new IllegalStateException("Return value transformer already set");
        }
        this.returnValueTransformer = new TransformerMetadata(clazz, methodName, TransformerType.RETURN_VALUE);
        return this;
    }

    @Override
    public WeldInvokerBuilder<T> withExceptionTransformer(Class<?> clazz, String methodName) {
        if (exceptionTransformer != null) {
            // TODO better exception, use Logger interface
            throw new IllegalStateException("Exception transformer already set");
        }
        this.exceptionTransformer = new TransformerMetadata(clazz, methodName, TransformerType.EXCEPTION);
        return this;
    }

    @Override
    public WeldInvokerBuilder<T> withInvocationWrapper(Class<?> clazz, String methodName) {
        if (invocationWrapper != null) {
            // TODO better exception, use Logger interface
            throw new IllegalStateException("Invocation wrapper already set");
        }
        this.invocationWrapper = new TransformerMetadata(clazz, methodName, TransformerType.WRAPPER);
        return this;
    }

    private boolean requiresCleanup() {
        boolean isStaticMethod = method.isStatic();
        if (instanceTransformer != null && !isStaticMethod) {
            return true;
        }
        for (int i = 0; i < argTransformers.length; i++) {
            if (argTransformers[i] != null) {
                return true;
            }
        }
        if (instanceLookup && !isStaticMethod) {
            return true;
        }
        for (int i = 0; i < argLookup.length; i++) {
            if (argLookup[i]) {
                return true;
            }
        }
        return false;
    }

    InvokerImpl<B, ?> doBuild() {
        Class<B> reflectionBeanClass = beanClass.getJavaClass();
        Method reflectionMethod = method.getReflection();

        boolean isStaticMethod = method.isStatic();
        int instanceArguments = isStaticMethod ? 0 : 1;
        boolean requiresCleanup = requiresCleanup();

        MethodHandle mh = MethodHandleUtils.createMethodHandle(reflectionMethod);

        // single, array-typed parameter at the end for variable arity methods
        mh = mh.asFixedArity();

        // instance transformer
        if (instanceTransformer != null && !isStaticMethod) {
            MethodHandle instanceTransformerMethod = MethodHandleUtils.createMethodHandleFromTransformer(reflectionMethod,
                    instanceTransformer, reflectionBeanClass);
            if (instanceTransformerMethod.type().parameterCount() == 1) { // no cleanup
                mh = MethodHandles.filterArguments(mh, 0, instanceTransformerMethod);
            } else if (instanceTransformerMethod.type().parameterCount() == 2) { // cleanup
                instanceTransformerMethod = instanceTransformerMethod
                        .asType(instanceTransformerMethod.type().changeParameterType(1, CleanupActions.class));
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
            MethodHandle argTransformerMethod = MethodHandleUtils.createMethodHandleFromTransformer(reflectionMethod,
                    argTransformers[i], reflectionMethod.getParameterTypes()[i]);
            if (argTransformerMethod.type().parameterCount() == 1) { // no cleanup
                mh = MethodHandles.filterArguments(mh, position, argTransformerMethod);
            } else if (argTransformerMethod.type().parameterCount() == 2) { // cleanup
                argTransformerMethod = argTransformerMethod
                        .asType(argTransformerMethod.type().changeParameterType(1, CleanupActions.class));
                mh = MethodHandles.collectArguments(mh, position, argTransformerMethod);
            } else {
                // internal error, this should not pass validation
                throw new IllegalStateException("Invalid argument transformer method: " + argTransformers[i]);
            }
        }

        // return type transformer
        if (returnValueTransformer != null) {
            MethodHandle returnValueTransformerMethod = MethodHandleUtils.createMethodHandleFromTransformer(reflectionMethod,
                    returnValueTransformer, reflectionMethod.getReturnType());
            mh = MethodHandles.filterReturnValue(mh, returnValueTransformerMethod);
        }

        // exception transformer
        if (exceptionTransformer != null) {
            MethodHandle exceptionTransformerMethod = MethodHandleUtils.createMethodHandleFromTransformer(reflectionMethod,
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
        if (requiresCleanup) {
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

        MethodType typeBeforeLookups = mh.type();
        int positionsBeforeArguments = 1; // first `CleanupActions` we need to preserve for transformations
        if (!isStaticMethod) {
            positionsBeforeArguments++; // the target instance
        }

        // instance lookup
        if (instanceLookup && !isStaticMethod) {
            Class<?> parameterType = typeBeforeLookups.parameterType(1);
            Type type = reflectionBeanClass;
            Annotation[] qualifiers = LookupUtils.extractQualifiers(beanClass.getAnnotations(), beanManager);
            MethodHandle instanceLookupMethod = MethodHandleUtils.LOOKUP;
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
            Class<?> parameterType = typeBeforeLookups.parameterType(i + (isStaticMethod ? 1 : 2));
            Type type = reflectionMethod.getParameters()[i].getParameterizedType();
            Annotation[] qualifiers = LookupUtils.extractQualifiers(method.getParameterAnnotations(i), beanManager);
            MethodHandle argumentLookupMethod = MethodHandleUtils.LOOKUP;
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
        if (requiresCleanup) {
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
        if (requiresCleanup) {
            MethodHandle cleanupMethod = mh.type().returnType() == void.class
                    ? MethodHandleUtils.CLEANUP_FOR_VOID
                    : MethodHandleUtils.CLEANUP_FOR_NONVOID;

            if (mh.type().returnType() != void.class) {
                cleanupMethod = cleanupMethod.asType(cleanupMethod.type()
                        .changeReturnType(mh.type().returnType())
                        .changeParameterType(1, mh.type().returnType()));
            }

            mh = MethodHandles.tryFinally(mh, cleanupMethod);
        }

        // spread argument array into individual arguments
        // keep leading arguments:   `CleanupAction` if needed   target instance if exists
        int leadingArgumentsToKeep = (requiresCleanup ? 1 : 0) + (isStaticMethod ? 0 : 1);
        if (isStaticMethod) {
            MethodHandle invoker = MethodHandles.spreadInvoker(mh.type(), leadingArgumentsToKeep);
            invoker = MethodHandles.insertArguments(invoker, 0, mh);
            invoker = MethodHandles.dropArguments(invoker, requiresCleanup ? 1 : 0, Object.class);
            mh = invoker;
        } else {
            MethodHandle invoker = MethodHandles.spreadInvoker(mh.type(), leadingArgumentsToKeep);
            invoker = MethodHandles.insertArguments(invoker, 0, mh);
            mh = invoker;
        }

        // replace `null` values in the arguments array with zero values
        // on positions where the method has an argument lookup configured
        // (this is just to prevent a NPE in method handles internals)
        Class<?>[] parameterTypes = reflectionMethod.getParameterTypes();
        if (LookupUtils.hasPrimitiveArgLookup(parameterTypes, argLookup)) {
            MethodHandle replaceNulls = MethodHandleUtils.REPLACE_PRIMITIVE_LOOKUP_NULLS;
            replaceNulls = MethodHandles.insertArguments(replaceNulls, 1, parameterTypes, argLookup);
            mh = MethodHandles.filterArguments(mh, requiresCleanup ? 2 : 1, replaceNulls);
        }

        // trim argument array if needed
        MethodHandle trimArgumentArray = MethodHandles.insertArguments(MethodHandleUtils.TRIM_ARRAY_TO_SIZE,
                1, reflectionMethod.getParameterCount());
        mh = MethodHandles.filterArguments(mh, requiresCleanup ? 2 : 1, trimArgumentArray);

        // instantiate `CleanupActions`
        if (requiresCleanup) {
            mh = MethodHandles.foldArguments(mh, MethodHandleUtils.CLEANUP_ACTIONS_CTOR);
        }

        // create an inner invoker and pass it to wrapper
        if (invocationWrapper != null) {
            InvokerImpl<?, ?> invoker = new InvokerImpl<>(mh);

            MethodHandle invocationWrapperMethod = MethodHandleUtils.createMethodHandleFromTransformer(reflectionMethod,
                    invocationWrapper, reflectionBeanClass);

            mh = MethodHandles.insertArguments(invocationWrapperMethod, 2, invoker);
        }

        return new InvokerImpl<>(mh);
    }

    // the following methods are exposed for deployment validation

    public AnnotatedType<B> getBeanClass() {
        return beanClass;
    }

    public TargetMethod getMethod() {
        return method;
    }

    public List<ConfiguredLookup> getConfiguredLookups() {
        List<ConfiguredLookup> result = new ArrayList<>();
        if (instanceLookup) {
            result.add(new ConfiguredLookup(-1, beanClass.getJavaClass(),
                    LookupUtils.extractQualifiers(beanClass.getAnnotations(), beanManager)));
        }
        for (int i = 0; i < argLookup.length; i++) {
            if (argLookup[i]) {
                result.add(new ConfiguredLookup(i, method.getParameterType(i),
                        LookupUtils.extractQualifiers(method.getParameterAnnotations(i), beanManager)));
            }
        }
        return result;
    }
}
