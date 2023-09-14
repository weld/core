package org.jboss.weld.tests.invokable.metadata.hierarchy;

import java.util.Collection;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;

public class ObservingExtension implements Extension {

    private Collection<AnnotatedMethod<? super Child>> childInvokableMethod;

    public void processBean1(@Observes ProcessManagedBean<Child> pmb) {
        childInvokableMethod = pmb.getInvokableMethods();
    }

    // TODO REMOVE
    // following two are not bean types
    public void processBean2(@Observes ProcessManagedBean<Parent> pmb) {
        throw new IllegalStateException("PMB for Parent invoked");
    }

    public void processBean3(@Observes ProcessManagedBean<CommonAncestor> pmb) {
        throw new IllegalStateException("PMB for CommonAncestror invoked");
    }

    public Collection<AnnotatedMethod<? super Child>> getChildInvokableMethod() {
        return childInvokableMethod;
    }
}
