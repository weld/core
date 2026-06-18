package org.jboss.weld.tests.event.wildcard.contravariant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanWithSimpleContravariantEvent {

    @Inject
    Event<? super Widget> widgetEvents;

    public void fireWidget(Widget widget) {
        widgetEvents.fire(widget);
    }
}
