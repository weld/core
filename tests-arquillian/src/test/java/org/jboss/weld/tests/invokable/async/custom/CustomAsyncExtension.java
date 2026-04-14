package org.jboss.weld.tests.invokable.async.custom;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.bootstrap.event.WeldProcessManagedBean;

public class CustomAsyncExtension implements Extension {

    private Invoker<CustomAsyncBean, ?> invoker;

    public void observeBean(@Observes WeldProcessManagedBean<CustomAsyncBean> pmb) {
        Collection<AnnotatedMethod<? super CustomAsyncBean>> methods = pmb.getAnnotatedBeanClass().getMethods();
        for (AnnotatedMethod<? super CustomAsyncBean> m : methods) {
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

    public Invoker<CustomAsyncBean, ?> getInvoker() {
        return invoker;
    }
}
