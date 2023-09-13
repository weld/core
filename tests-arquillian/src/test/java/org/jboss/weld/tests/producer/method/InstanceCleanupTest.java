package org.jboss.weld.tests.producer.method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InstanceCleanupTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InstanceCleanupTest.class))
                .addPackage(InstanceCleanupTest.class.getPackage())
                .addClass(Utils.class);
    }

    @Test
    public void testInstanceCleansUpDependents(BeanManagerImpl beanManager) {
        Kitchen.reset();

        Bean<Cafe> bean = Reflections.cast(beanManager.resolve(beanManager.getBeans(Cafe.class)));
        CreationalContext<Cafe> cc = beanManager.createCreationalContext(bean);

        Cafe instance = bean.create(cc);
        Food food = instance.getSalad();
        bean.destroy(instance, cc);

        assertNotNull(food);
        assertNotNull(Kitchen.getCompostedFood());
        assertTrue(Kitchen.getCompostedFood().isMade());
    }

}
