package org.jboss.weld.tests.invokable.async.wardedup;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;

public class WarDedupExtension implements Extension {

    private Invoker<WarDedupBean, ?> invoker;

    public void observeBean(@Observes ProcessManagedBean<WarDedupBean> pmb) {
        Collection<AnnotatedMethod<? super WarDedupBean>> methods = pmb.getAnnotatedBeanClass().getMethods();
        for (AnnotatedMethod<? super WarDedupBean> m : methods) {
            if ("hello".equals(m.getJavaMember().getName())) {
                invoker = pmb.createInvoker(m)
                        .withInstanceLookup()
                        .withArgumentLookup(0)
                        .build();
            }
        }
    }

    public void validate(@Observes AfterDeploymentValidation adv) {
        adv.ensureAsyncHandlerExists(MyAsyncType.class);
    }

    public Invoker<WarDedupBean, ?> getInvoker() {
        return invoker;
    }
}
