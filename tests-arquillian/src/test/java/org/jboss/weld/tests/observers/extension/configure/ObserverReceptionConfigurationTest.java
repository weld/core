package org.jboss.weld.tests.observers.extension.configure;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ObserverReceptionConfigurationTest {

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(ObserverReceptionConfigurationTest.class))
                .addPackage(ObserverReceptionConfigurationTest.class.getPackage())
                .addAsServiceProvider(Extension.class, MyExtension.class);
    }

    @Inject
    ObservingBean bean;

    @Inject
    Event<Foo> fooEvent;

    @Test
    public void testObserverReceptionChanged() {
        // assert initial state
        Assert.assertEquals(0, ObservingBean.timesObserved);

        // fire event and assert again, should not be notified
        fooEvent.fire(new Foo());
        Assert.assertEquals(0, ObservingBean.timesObserved);

        // trigger bean creation and repeat the test
        bean.ping();
        fooEvent.fire(new Foo());
        Assert.assertEquals(1, ObservingBean.timesObserved);
    }
}
