package org.jboss.weld.tests.interceptors.bridgemethods.hierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1672
 */
@RunWith(Arquillian.class)
public class InterceptorBridgeMethodTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptorBridgeMethodTest.class)).intercept(MissileInterceptor.class)
                .addPackage(InterceptorBridgeMethodTest.class.getPackage()).addClass(ActionSequence.class);
    }

    @Test
    public void testInterception(Child child) {
        reset();
        child.invoke("foo");
        verify();
        reset();
        Parent<String> parent = child;
        parent.invoke("foo");
        verify();
    }

    private void reset() {
        MissileInterceptor.intercepted = false;
        ActionSequence.reset();
    }

    private void verify() {
        assertTrue(MissileInterceptor.intercepted);
        assertEquals(1, ActionSequence.getSequenceSize());
        assertEquals(Child.class.getName(), ActionSequence.getSequenceData().get(0));
    }

}
