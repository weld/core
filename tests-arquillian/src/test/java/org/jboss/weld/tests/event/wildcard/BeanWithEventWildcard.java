package org.jboss.weld.tests.event.wildcard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanWithEventWildcard {

    @Inject
    Event<?> wildEvent;

}
