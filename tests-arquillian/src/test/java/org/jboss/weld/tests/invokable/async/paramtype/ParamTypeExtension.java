package org.jboss.weld.tests.invokable.async.paramtype;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.invoke.Invoker;

import org.jboss.weld.bootstrap.event.WeldProcessManagedBean;

public class ParamTypeExtension implements Extension {

    private Invoker<ParamTypeBean, ?> invoker;
    private Invoker<ParamTypeBean, ?> syncInvoker;
    private Invoker<ParamTypeBean, ?> noLookupInvoker;

    public void observeBean(@Observes WeldProcessManagedBean<ParamTypeBean> pmb) {
        Collection<AnnotatedMethod<? super ParamTypeBean>> methods = pmb.getAnnotatedBeanClass().getMethods();
        for (AnnotatedMethod<? super ParamTypeBean> m : methods) {
            String name = m.getJavaMember().getName();
            if ("hello".equals(name)) {
                invoker = pmb.createInvoker(m)
                        .withInstanceLookup()
                        .withArgumentLookup(0)
                        .build();
            } else if ("helloSync".equals(name)) {
                syncInvoker = pmb.createInvoker(m)
                        .withInstanceLookup()
                        .withArgumentLookup(0)
                        .build();
            } else if ("helloNoLookup".equals(name)) {
                noLookupInvoker = pmb.createInvoker(m).build();
            }
        }
    }

    public Invoker<ParamTypeBean, ?> getInvoker() {
        return invoker;
    }

    public Invoker<ParamTypeBean, ?> getSyncInvoker() {
        return syncInvoker;
    }

    public Invoker<ParamTypeBean, ?> getNoLookupInvoker() {
        return noLookupInvoker;
    }
}
