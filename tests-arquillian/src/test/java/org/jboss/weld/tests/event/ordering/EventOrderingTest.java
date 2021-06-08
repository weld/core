package org.jboss.weld.tests.event.ordering;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EventOrderingTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EventOrderingTest.class)).addPackage(EventOrderingTest.class.getPackage());
    }

    @Inject
    BeanManager manager;

    @Inject
    Event<EventPayload> event;

    @Test
    public void testOrdering() {

        EventPayload p1 = new EventPayload();
        manager.getEvent().select(EventPayload.class).fire(p1);

        p1.assertObservers(Alpha.class.getName(), Charlie.class.getName(), Bravo.class.getName(), NoPriority.class.getName());
        p1.reset();

        event.fire(p1);
        p1.assertObservers(Alpha.class.getName(), Charlie.class.getName(), Bravo.class.getName(), NoPriority.class.getName());
    }

}
