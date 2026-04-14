package org.jboss.weld.tests.invokable.async;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.bootstrap.event.WeldProcessManagedBean;

public class AsyncInvokerExtension implements Extension {

    private Invoker<AsyncBean, ?> csInvoker;
    private Invoker<AsyncBean, ?> cfInvoker;
    private Invoker<AsyncBean, ?> fpInvoker;

    public void observeAsyncBean(@Observes WeldProcessManagedBean<AsyncBean> pmb) {
        Collection<AnnotatedMethod<? super AsyncBean>> methods = pmb.getAnnotatedBeanClass().getMethods();
        for (AnnotatedMethod<? super AsyncBean> m : methods) {
            String name = m.getJavaMember().getName();
            if ("helloCS".equals(name)) {
                csInvoker = pmb.createInvoker(m)
                        .withInstanceLookup()
                        .withArgumentLookup(0)
                        .build();
            } else if ("helloCF".equals(name)) {
                cfInvoker = pmb.createInvoker(m)
                        .withInstanceLookup()
                        .withArgumentLookup(0)
                        .build();
            } else if ("helloFP".equals(name)) {
                fpInvoker = pmb.createInvoker(m)
                        .withInstanceLookup()
                        .withArgumentLookup(0)
                        .build();
            }
        }
    }

    public void validate(@Observes AfterDeploymentValidation adv) {
        adv.ensureAsyncHandlerExists(CompletionStage.class);
        adv.ensureAsyncHandlerExists(CompletableFuture.class);
        adv.ensureAsyncHandlerExists(Flow.Publisher.class);
    }

    public Invoker<AsyncBean, ?> getCsInvoker() {
        return csInvoker;
    }

    public Invoker<AsyncBean, ?> getCfInvoker() {
        return cfInvoker;
    }

    public Invoker<AsyncBean, ?> getFpInvoker() {
        return fpInvoker;
    }
}
