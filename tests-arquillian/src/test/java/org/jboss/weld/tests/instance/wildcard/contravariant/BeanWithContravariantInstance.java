package org.jboss.weld.tests.instance.wildcard.contravariant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanWithContravariantInstance {

    @Inject
    Instance<? super Widget> contravariantInstance;
}
