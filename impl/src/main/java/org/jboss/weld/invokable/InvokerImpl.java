package org.jboss.weld.invokable;

import jakarta.enterprise.context.Dependent;
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    private final InvokerCleanupActions cleanupActions;

    // this variant is only used for invocation wrapper and assumes fully-initialized state
    private InvokerImpl(MethodHandle beanMethodHandle, Annotation[] instanceLookupQualifiers, Annotation[][] argLookupQualifiers,
                        boolean hasInstanceTransformer, InvokerCleanupActions cleanupActions,
                        Class<T> beanClass, Method method) {
        this.beanMethodHandle = beanMethodHandle;
        this.instanceLookupQualifiers = instanceLookupQualifiers;
        this.argLookupQualifiers = argLookupQualifiers;
        this.hasInstanceTransformer = hasInstanceTransformer;
        this.cleanupActions = cleanupActions;
        this.beanClass = beanClass;
        this.method = method;
        this.invocationWrapper = null;
    }

    InvokerImpl(AbstractInvokerBuilder<T, ?> builder) {
        // needs to be initialized first, as can be used when creating method handles
        this.cleanupActions = new InvokerCleanupActions();
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
                builder.method, builder.invocationWrapper, builder.beanClass, cleanupActions);

        boolean isStaticMethod = Modifier.isStatic(builder.method.getModifiers());

        MethodHandle finalMethodHandle = MethodHandleUtils.createMethodHandle(builder.method);

        // instance transformer
        if (builder.instanceTransformer != null && !isStaticMethod) {
            MethodHandle instanceTransformer = MethodHandleUtils.createMethodHandleFromTransformer(builder.method,
                    builder.instanceTransformer, builder.beanClass, cleanupActions);
            // instance is the first argument of the method handle
            finalMethodHandle = MethodHandles.filterArguments(finalMethodHandle, 0, instanceTransformer);
        }

        // argument transformers
        {
            // `null` elements of this array are treated as identity functions, see `filterArguments`
            MethodHandle[] argTransformers = new MethodHandle[builder.argTransformers.length];
            for (int i = 0; i < builder.argTransformers.length; i++) {
                if (builder.argTransformers[i] != null) {
                    argTransformers[i] = MethodHandleUtils.createMethodHandleFromTransformer(builder.method,
                            builder.argTransformers[i], builder.method.getParameterTypes()[i], cleanupActions);
                }
            }
            // in case of non-`static` methods, the first argument of the method handle is the instance
            finalMethodHandle = MethodHandles.filterArguments(finalMethodHandle, isStaticMethod ? 0 : 1, argTransformers);
        }

        // return type transformer
        if (builder.returnValueTransformer != null) {
            MethodHandle returnValueTransformer = MethodHandleUtils.createMethodHandleFromTransformer(builder.method,
                    builder.returnValueTransformer, builder.method.getReturnType(), cleanupActions);
            finalMethodHandle = MethodHandles.filterReturnValue(finalMethodHandle, returnValueTransformer);
        }

        // exception transformer
        if (builder.exceptionTransformer != null) {
            MethodHandle exceptionTransformer = MethodHandleUtils.createMethodHandleFromTransformer(builder.method,
                    builder.exceptionTransformer, Throwable.class, cleanupActions);
            finalMethodHandle = MethodHandles.catchException(finalMethodHandle, Throwable.class, exceptionTransformer);
        }

        // spread argument array into individual arguments
        if (isStaticMethod) {
            MethodHandle invoker = MethodHandles.spreadInvoker(finalMethodHandle.type(), 0);
            invoker = MethodHandles.insertArguments(invoker, 0, finalMethodHandle);
            invoker = MethodHandles.dropArguments(invoker, 0, Object.class);
            finalMethodHandle = invoker;
        } else {
            MethodHandle invoker = MethodHandles.spreadInvoker(finalMethodHandle.type(), 1);
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
                        argLookupQualifiers, hasInstanceTransformer, cleanupActions, beanClass, method));
            } catch (Throwable e) {
                // invocation wrapper or the method itself threw an exception, we just rethrow
                throw new RuntimeException(e);
            }
        }

        // TODO do we want to verify that the T instance is the exact class of the bean?
        if (arguments.length != method.getParameterCount()) {
            // TODO per spec, excess arguments should be dropped
            throw new IllegalArgumentException("Wrong number of args for invoker! Expected: " + method.getParameterCount() + " Provided: " + arguments.length);
        }
        Instance<Object> cdiLookup = CDI.current().getBeanManager().createInstance();
        if (instanceLookupQualifiers != null) {
            // instance lookup set, ignore the parameter we got and lookup the instance
            // standard CDI resolution errors can occur
            instance = getInstance(cdiLookup, beanClass, instanceLookupQualifiers);
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
                arguments[i] = getInstance(cdiLookup, method.getParameterTypes()[i], argLookupQualifiers[i]);
            }
        }

        try {
            return (R) beanMethodHandle.invoke(instance, arguments);
        } catch (ValueCarryingException e) {
            // exception transformer may return a value by throwing a special exception
            return (R) e.getMethodReturnValue();
        } catch (Throwable e) {
            // TODO at this point there might have been exception transformer invoked as well, guess we just rethrow?
            // we just rethrow the original exception
            throw new RuntimeException(e);
        } finally {
            // invoke cleanup tasks running all registered consumers as well as destroying dependent beans
            this.cleanupActions.cleanup();
        }
    }

    private <TYPE> TYPE getInstance(Instance<Object> lookup, Class<TYPE> classToLookup, Annotation... qualifiers) {
        Instance.Handle<TYPE> handle = lookup.select(classToLookup, qualifiers).getHandle();
        this.cleanupActions.addInstanceHandle(handle);
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

    private static class InvokerCleanupActions implements Consumer<Runnable> {
        private final List<Runnable> cleanupTasks = new ArrayList<>();
        private final List<Instance.Handle<?>> dependentInstances = new ArrayList<>();

        @Override
        public void accept(Runnable runnable) {
            cleanupTasks.add(runnable);
        }

        public void addInstanceHandle(Instance.Handle<?> handle) {
            if (handle.getBean().getScope().equals(Dependent.class)) {
                dependentInstances.add(handle);
            }
        }

        public void cleanup() {
            // run all registered tasks
            for (Runnable r : cleanupTasks) {
                r.run();
            }
            cleanupTasks.clear();

            // destroy dependent beans we created
            for (Instance.Handle<?> handle : dependentInstances) {
                handle.destroy();
            }
            dependentInstances.clear();
        }
    }
}
