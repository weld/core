package org.jboss.weld.literal;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Veto;
import javax.enterprise.util.TypeLiteral;

@Veto
public class EventLiteral<T> extends TypeLiteral<Event<T>> {

    private static final long serialVersionUID = 450443493114073397L;

}
