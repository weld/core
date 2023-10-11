package org.jboss.weld.tests.invokable.transformers.input;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;

import org.junit.Assert;

public class ObservingExtension implements Extension {

    Invoker<ActualBean, ?> noTransformer;
    Invoker<ActualBean, ?> transformInstance1;
    Invoker<ActualBean, ?> transformInstance2;
    Invoker<ActualBean, ?> transformArg1;
    Invoker<ActualBean, ?> transformArg2;

    public Invoker<ActualBean, ?> getTransformInstance1() {
        return transformInstance1;
    }

    public Invoker<ActualBean, ?> getTransformInstance2() {
        return transformInstance2;
    }

    public Invoker<ActualBean, ?> getTransformArg1() {
        return transformArg1;
    }

    public Invoker<ActualBean, ?> getTransformArg2() {
        return transformArg2;
    }

    public Invoker<ActualBean, ?> getNoTransformer() {
        return noTransformer;
    }

    public void observe(@Observes ProcessManagedBean<ActualBean> pmb) {
        Collection<AnnotatedMethod<? super ActualBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(2, invokableMethods.size());
        for (AnnotatedMethod<? super ActualBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("ping")) {
                noTransformer = pmb.createInvoker(invokableMethod).build();
                transformInstance1 = pmb.createInvoker(invokableMethod)
                        .setInstanceTransformer(Transformer.class, "transformInstance1")
                        .build();
                transformInstance2 = pmb.createInvoker(invokableMethod)
                        .setInstanceTransformer(Transformer.class, "transformInstance2")
                        .build();

                transformArg1 = pmb.createInvoker(invokableMethod)
                        .setArgumentTransformer(0, Transformer.class, "transformArg1")
                        .build();
                transformArg2 = pmb.createInvoker(invokableMethod)
                        .setArgumentTransformer(0, Transformer.class, "transformArg2")
                        .build();
            }
        }
    }
}
