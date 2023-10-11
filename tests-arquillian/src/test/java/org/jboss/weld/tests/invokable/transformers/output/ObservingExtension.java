package org.jboss.weld.tests.invokable.transformers.output;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;

import org.junit.Assert;

public class ObservingExtension implements Extension {

    Invoker<ActualBean, ?> noTransformer;
    Invoker<ActualBean, ?> transformReturnType1;
    Invoker<ActualBean, ?> transformReturnType2;
    Invoker<ExceptionalBean, ?> transformException1;
    Invoker<ExceptionalBean, ?> transformException2;
    Invoker<ExceptionalBean, ?> transformException3;

    public Invoker<ActualBean, ?> getTransformReturnType1() {
        return transformReturnType1;
    }

    public Invoker<ActualBean, ?> getTransformReturnType2() {
        return transformReturnType2;
    }

    public Invoker<ExceptionalBean, ?> getTransformException1() {
        return transformException1;
    }

    public Invoker<ExceptionalBean, ?> getTransformException2() {
        return transformException2;
    }

    public Invoker<ExceptionalBean, ?> getTransformException3() {
        return transformException3;
    }

    public Invoker<ActualBean, ?> getNoTransformer() {
        return noTransformer;
    }

    public void observe(@Observes ProcessManagedBean<ActualBean> pmb) {
        Collection<AnnotatedMethod<? super ActualBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(1, invokableMethods.size());
        AnnotatedMethod<? super ActualBean> invokableMethod = invokableMethods.iterator().next();
        noTransformer = pmb.createInvoker(invokableMethod).build();
        transformReturnType1 = pmb.createInvoker(invokableMethod)
                .setReturnValueTransformer(Transformer.class, "transformReturn1")
                .build();
        transformReturnType2 = pmb.createInvoker(invokableMethod)
                .setReturnValueTransformer(Transformer.class, "transformReturn2")
                .build();
    }

    public void observeExceptionally(@Observes ProcessManagedBean<ExceptionalBean> pmb) {
        Collection<AnnotatedMethod<? super ExceptionalBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(1, invokableMethods.size());
        AnnotatedMethod<? super ExceptionalBean> invokableMethod = invokableMethods.iterator().next();

        transformException1 = pmb.createInvoker(invokableMethod)
                .setExceptionTransformer(Transformer.class, "transformException1")
                .build();
        transformException2 = pmb.createInvoker(invokableMethod)
                .setExceptionTransformer(Transformer.class, "transformException2")
                .build();
        transformException3 = pmb.createInvoker(invokableMethod)
                .setExceptionTransformer(Transformer.class, "transformException3")
                .build();
    }
}
