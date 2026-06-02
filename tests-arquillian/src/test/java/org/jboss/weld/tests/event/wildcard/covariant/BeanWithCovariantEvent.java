package org.jboss.weld.tests.event.wildcard.covariant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanWithCovariantEvent {

    @Inject
    Event<? extends Widget> covariantEvent;
}
