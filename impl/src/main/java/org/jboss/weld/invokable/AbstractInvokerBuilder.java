package org.jboss.weld.invokable;

import jakarta.enterprise.invoke.InvokerBuilder;

import java.lang.reflect.Method;

// TODO add exception on repeated invocations of builder methods as per spec
abstract class AbstractInvokerBuilder<T> implements InvokerBuilder<T> {

    final Class<T> beanClass;
    // work with reflection representation so that we can re-use this logic from within BCE
    final Method method;

    boolean instanceLookup;
    boolean[] argLookup;
    TransformerMetadata instanceTransformer;
    TransformerMetadata returnValueTransformer;
    TransformerMetadata exceptionTransformer;
    TransformerMetadata invocationWrapper;
    final TransformerMetadata[] argTransformers;

    // TODO Class is rawtype otherwise we cannot use it from InvokerInfoBuilder, can we improve this?
    public AbstractInvokerBuilder(Class beanClass, Method method) {
        this.beanClass = beanClass;
        this.method = method;
        this.argLookup = new boolean[method.getParameters().length];
        this.argTransformers = new TransformerMetadata[method.getParameters().length];
    }


    @Override
    public InvokerBuilder<T> setInstanceLookup() {
        this.instanceLookup = true;
        return this;
    }

    @Override
    public InvokerBuilder<T> setArgumentLookup(int i) {
        if (i >= argLookup.length) {
            // TODO better exception
            throw new IllegalArgumentException("Error attempting to set CDI argument lookup for arg number " + i + " while the number of method args is " + argLookup.length);
        }
        argLookup[i] = true;
        return this;
    }

    @Override
    public InvokerBuilder<T> setInstanceTransformer(Class<?> aClass, String s) {
        this.instanceTransformer = new TransformerMetadata(aClass, s, TransformerType.INSTANCE);
        return this;
    }

    @Override
    public InvokerBuilder<T> setArgumentTransformer(int i, Class<?> aClass, String s) {
        if (i >= argLookup.length) {
            // TODO better exception
            throw new IllegalArgumentException("Error attempting to set an argument lookup. Number of method args: " + argLookup.length + " arg position: " + i);
        }
        this.argTransformers[i] = new TransformerMetadata(aClass, s, TransformerType.ARGUMENT);
        return this;
    }

    @Override
    public InvokerBuilder<T> setReturnValueTransformer(Class<?> aClass, String s) {
        this.returnValueTransformer = new TransformerMetadata(aClass, s, TransformerType.RETURN_VALUE);
        return this;
    }

    @Override
    public InvokerBuilder<T> setExceptionTransformer(Class<?> aClass, String s) {
        this.exceptionTransformer = new TransformerMetadata(aClass, s, TransformerType.EXCEPTION);
        return this;
    }

    @Override
    public InvokerBuilder<T> setInvocationWrapper(Class<?> aClass, String s) {
        this.invocationWrapper = new TransformerMetadata(aClass, s, TransformerType.WRAPPER);
        return this;
    }
}
