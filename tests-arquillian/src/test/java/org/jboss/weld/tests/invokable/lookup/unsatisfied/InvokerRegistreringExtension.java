package org.jboss.weld.tests.invokable.lookup.unsatisfied;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;

public class InvokerRegistreringExtension implements Extension {

    private Invoker<InvokableBean, ?> unsatisfiedLookupInvoker;

    public Invoker<InvokableBean, ?> getUnsatisfiedLookupInvoker() {
        return unsatisfiedLookupInvoker;
    }

    public void createInvokers(@Observes ProcessManagedBean<InvokableBean> pmb) {
        Collection<AnnotatedMethod<? super InvokableBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        if (invokableMethods.size() != 1) {
            throw new IllegalStateException("There should be exactly one method");
        }
        unsatisfiedLookupInvoker = pmb.createInvoker(invokableMethods.iterator().next()).withArgumentLookup(0).build();
    }
}
