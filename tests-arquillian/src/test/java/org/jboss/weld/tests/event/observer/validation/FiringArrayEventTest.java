package org.jboss.weld.tests.event.observer.validation;

import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:borisha.zivkovic@gmail.com">Borisa Zivkovic</a>
 */
@RunWith(Arquillian.class)
public class FiringArrayEventTest {

    @Inject
    private BeanManager manager;

    @Inject
    private ArrayObserverBean observerBean;

    @Inject
    private Event<int[]> arrayEvent;

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class, "weld_events.jar").addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(ArrayObserverBean.class);
    }

    @Before
    public void setup() {
        this.observerBean.reset();
    }

    @Test
    public void testResolverArray() {

        final Set<ObserverMethod<? super int[]>> observers = this.manager.resolveObserverMethods(new int[] {});

        Assert.assertEquals("should have one observer", 1, observers.size());
        Assert.assertFalse("should have not received update", this.observerBean.isReceivedUpdate());
        Assert.assertNull("should have not received update", this.observerBean.getData());

        int[] data = new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE };

        for (final ObserverMethod<? super int[]> observer : observers) {
            observer.notify(data);
        }

        Assert.assertTrue("should have received update", this.observerBean.isReceivedUpdate());
        Assert.assertArrayEquals("should have received update", this.observerBean.getData(), data);

    }

    @Test
    public void testEventArray() {

        Assert.assertFalse("should have not received update", this.observerBean.isReceivedUpdate());
        Assert.assertNull("should have not received update", this.observerBean.getData());

        int[] data = new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE };

        this.arrayEvent.fire(data);
        // should not fail, this test should behave same as test_resolver_array()
        Assert.assertTrue("should have received update", this.observerBean.isReceivedUpdate());
        Assert.assertArrayEquals("should have received update", this.observerBean.getData(), data);

    }

}
