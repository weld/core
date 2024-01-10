package org.jboss.weld.tests.invokable.lookup;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;

import org.junit.Assert;

public class InvokerRegistreringExtension implements Extension {

    private Invoker<InvokableBean, ?> instanceLookupInvoker;
    private Invoker<InvokableBean, ?> correctLookupInvoker;
    private Invoker<InvokableBean, ?> lookupWithRegisteredQualifier;

    public Invoker<InvokableBean, ?> getInstanceLookupInvoker() {
        return instanceLookupInvoker;
    }

    public Invoker<InvokableBean, ?> getCorrectLookupInvoker() {
        return correctLookupInvoker;
    }

    public Invoker<InvokableBean, ?> getLookupWithRegisteredQualifier() {
        return lookupWithRegisteredQualifier;
    }

    public void createInvokers(@Observes ProcessManagedBean<InvokableBean> pmb) {
        Collection<AnnotatedMethod<? super InvokableBean>> invokableMethods = pmb.getAnnotatedBeanClass().getMethods();
        Assert.assertEquals(3, invokableMethods.size());
        for (AnnotatedMethod<? super InvokableBean> invokableMethod : invokableMethods) {
            if (invokableMethod.getJavaMember().getName().contains("instanceLookup")) {
                instanceLookupInvoker = pmb.createInvoker(invokableMethod).withInstanceLookup().build();
            } else if (invokableMethod.getJavaMember().getName().contains("lookupWithRegisteredQualifier")) {
                lookupWithRegisteredQualifier = pmb.createInvoker(invokableMethod).withArgumentLookup(0).build();
            } else {
                correctLookupInvoker = pmb.createInvoker(invokableMethod).withArgumentLookup(0).withArgumentLookup(1).build();
            }
        }
    }

    public void registerQualifier(@Observes BeforeBeanDiscovery bbd) {
        bbd.addQualifier(ToBeQualifier.class);
    }
}
