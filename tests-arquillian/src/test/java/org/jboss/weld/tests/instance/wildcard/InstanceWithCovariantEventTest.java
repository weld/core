package org.jboss.weld.tests.instance.wildcard;

import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instance<? extends Event<String>> should be allowed since Event<String> is a valid Event injection point.
 */
@RunWith(Arquillian.class)
public class InstanceWithCovariantEventTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InstanceWithCovariantEventTest.class))
                .addClass(BeanWithCovariantEventInstance.class);
    }

    @Test
    public void testInstanceWithCovariantEvent(BeanWithCovariantEventInstance bean) {
        assertNotNull(bean);
    }
}
