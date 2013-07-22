package org.jboss.weld.tests.lifecycle.overridden;

import static org.junit.Assert.assertEquals;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
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

    @Inject
    private BeanManager beanManager;


    @Test
    public void testPostConstructOfBeanWithAnnotationIsCalledExactlyOnce() {
        assertEquals(1, beanWithAnnotation.getPostConstructInvocationCount());
    }

    @Test
    public void testPostConstructOfBeanWithoutAnnotationIsNotCalled() {
        assertEquals(0, beanWithoutAnnotation.getPostConstructInvocationCount());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPreDestroyOfBeanWithAnnotationIsCalledExactlyOnce() {
        for (Bean b : beanManager.getBeans(OverridingBeanWithAnnotation.class)) {
            CreationalContext ctx = beanManager.createCreationalContext(null);
            OverridingBeanWithAnnotation instance = (OverridingBeanWithAnnotation) beanManager.getReference(b, b.getBeanClass(), ctx);
            b.destroy(instance, ctx);
            assertEquals(1, instance.getPreDestroyInvocationCount());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPreDestroyOfBeanWithoutAnnotationIsNotCalled() {
        for (Bean b : beanManager.getBeans(OverridingBeanWithoutAnnotation.class)) {
            CreationalContext ctx = beanManager.createCreationalContext(null);
            OverridingBeanWithoutAnnotation instance = (OverridingBeanWithoutAnnotation) beanManager.getReference(b, b.getBeanClass(), ctx);
            b.destroy(instance, ctx);
            assertEquals(0, instance.getPreDestroyInvocationCount());
        }
    }

}
