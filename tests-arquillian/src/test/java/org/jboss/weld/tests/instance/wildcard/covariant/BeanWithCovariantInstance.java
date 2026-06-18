package org.jboss.weld.tests.instance.wildcard.covariant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanWithCovariantInstance {

    @Inject
    Instance<? extends Widget> covariantInstance;

    public boolean isResolvable() {
        return covariantInstance.isResolvable();
    }

    public Widget get() {
        return covariantInstance.get();
    }
}
