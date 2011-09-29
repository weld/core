package org.jboss.weld.tests.producer.method;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.util.AnnotationLiteral;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class InstanceCleanupTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(InstanceCleanupTest.class.getPackage())
                .addClass(Utils.class);
    }

    @Test
    public void testInstanceCleansUpDependents(BeanManagerImpl beanManager) {
        Kitchen.reset();
        Food food = Utils.getReference(beanManager, Food.class, new AnnotationLiteral<Compostable>() {
        });
        assertNotNull(food);
        assertNotNull(Kitchen.getCompostedFood());
        assertTrue(Kitchen.getCompostedFood().isMade());
    }

}
