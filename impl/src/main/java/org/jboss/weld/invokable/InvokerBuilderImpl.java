package org.jboss.weld.invokable;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.invoke.InvokerBuilder;
import org.jboss.weld.bean.ClassBean;

// TODO add exception on repeated invocations of builder methods as per spec
public class InvokerBuilderImpl<T> implements InvokerBuilder<Invoker<T, ?>> {

    final ClassBean<T> classBean;
    final AnnotatedMethod<? super T> method;

    boolean instanceLookup;
    boolean[] argLookup;
    TransformerMetadata instanceTransformer;
    TransformerMetadata returnValueTransformer;
    TransformerMetadata exceptionTransformer;
    TransformerMetadata invocationWrapper;
    final TransformerMetadata[] argTransformers;

    public InvokerBuilderImpl(ClassBean<T> classBean, AnnotatedMethod<? super T> method) {
        this.classBean = classBean;
        this.method = method;
        this.argLookup = new boolean[method.getParameters().size()];
        this.argTransformers = new TransformerMetadata[method.getParameters().size()];
    }


    @Override
    public InvokerBuilder<Invoker<T, ?>> setInstanceLookup() {
        this.instanceLookup = true;
        return this;
    }

    @Override
    public InvokerBuilder<Invoker<T, ?>> setArgumentLookup(int i) {
        if (i >= argLookup.length) {
            // TODO better exception
            throw new IllegalArgumentException("Error attempting to set CDI argument lookup for arg number " + i + " while the number of method args is " + argLookup.length);
        }
        argLookup[i] = true;
        return this;
    }

    @Override
    public InvokerBuilder<Invoker<T, ?>> setInstanceTransformer(Class<?> aClass, String s) {
        this.instanceTransformer = new TransformerMetadata(aClass, s, TransformerType.INSTANCE);
        return this;
    }

    @Override
    public InvokerBuilder<Invoker<T, ?>> setArgumentTransformer(int i, Class<?> aClass, String s) {
        if (i >= argLookup.length) {
            // TODO better exception
            throw new IllegalArgumentException("Error attempting to set an argument lookup. Number of method args: " + argLookup.length + " arg position: " + i);
        }
        this.argTransformers[i] = new TransformerMetadata(aClass, s, TransformerType.ARGUMENT);
        return this;
    }

    @Override
    public InvokerBuilder<Invoker<T, ?>> setReturnValueTransformer(Class<?> aClass, String s) {
        this.returnValueTransformer = new TransformerMetadata(aClass, s, TransformerType.RETURN_VALUE);
        return this;
    }

    @Override
    public InvokerBuilder<Invoker<T, ?>> setExceptionTransformer(Class<?> aClass, String s) {
        this.exceptionTransformer = new TransformerMetadata(aClass, s, TransformerType.EXCEPTION);
        return this;
    }

    @Override
    public InvokerBuilder<Invoker<T, ?>> setInvocationWrapper(Class<?> aClass, String s) {
        this.invocationWrapper = new TransformerMetadata(aClass, s, TransformerType.WRAPPER);
        return this;
    }

    @Override
    public Invoker<T, ?> build() {
        return new InvokerImpl<>(this);
    }
}
