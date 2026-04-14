package org.jboss.weld.tests.invokable.async.paramtype;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.bootstrap.event.WeldProcessManagedBean;

public class ParamTypeExtension implements Extension {

    private Invoker<ParamTypeBean, ?> invoker;

    public void observeBean(@Observes WeldProcessManagedBean<ParamTypeBean> pmb) {
        Collection<AnnotatedMethod<? super ParamTypeBean>> methods = pmb.getAnnotatedBeanClass().getMethods();
        for (AnnotatedMethod<? super ParamTypeBean> m : methods) {
            if ("hello".equals(m.getJavaMember().getName())) {
                invoker = pmb.createInvoker(m)
                        .withInstanceLookup()
                        .withArgumentLookup(0)
                        .build();
            }
        }
    }

    public Invoker<ParamTypeBean, ?> getInvoker() {
        return invoker;
    }
}
