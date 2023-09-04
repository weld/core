package org.jboss.weld.invokable;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.invoke.InvokerBuilder;

import java.lang.reflect.Method;

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
}
