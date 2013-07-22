package org.jboss.weld.tests.lifecycle.overridden;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(Arquillian.class)
public class OverriddenLifecycleMethodTest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class)
            .addPackage(OverriddenLifecycleMethodTest.class.getPackage());
    }

    @Inject
    private OverridingBeanWithAnnotation beanWithAnnotation;

    @Inject
    private OverridingBeanWithoutAnnotation beanWithoutAnnotation;

    @Test
    public void testBeanWithAnnotationIsCalledExactlyOnce() {
        assertEquals(1, beanWithAnnotation.getPostConstructInvocationCount());
    }

    @Test
    public void testBeanWithoutAnnotationIsNotCalled() {
        assertEquals(0, beanWithoutAnnotation.getPostConstructInvocationCount());
    }

}
