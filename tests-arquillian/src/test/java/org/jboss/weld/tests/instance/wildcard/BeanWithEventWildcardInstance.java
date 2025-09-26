package org.jboss.weld.tests.instance.wildcard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanWithEventWildcardInstance {

    @Inject
    Instance<Event<?>> sneakyWildcard;

}
