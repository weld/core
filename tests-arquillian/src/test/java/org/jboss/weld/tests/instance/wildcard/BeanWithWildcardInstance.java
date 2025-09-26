package org.jboss.weld.tests.instance.wildcard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanWithWildcardInstance {
    @Inject
    Instance<?> wildInstance;
}
