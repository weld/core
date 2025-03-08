package org.jboss.weld.tests.invokable.exceptions;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.bootstrap.event.WeldProcessManagedBean;
import org.jboss.weld.tests.invokable.common.SimpleBean;

public class ObservingExtension implements Extension {

    private Invoker<SimpleBean, ?> invoker;

    public Invoker<SimpleBean, ?> getInvoker() {
        return invoker;
    }

    public void createInvoker(@Observes WeldProcessManagedBean<SimpleBean> pmb) {
        AnnotatedMethod<? super SimpleBean> pingMethod = pmb.getAnnotatedBeanClass().getMethods().stream()
                .filter(m -> m.getJavaMember().getName().equals("ping")).findFirst().get();
        invoker = pmb.createInvoker(pingMethod).build();
    }
}
