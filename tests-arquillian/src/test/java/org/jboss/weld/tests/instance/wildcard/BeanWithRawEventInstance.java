package org.jboss.weld.tests.instance.wildcard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanWithRawEventInstance {
    @Inject
    @SuppressWarnings("rawtypes")
    Instance<Event> rawEventInstance;
}
