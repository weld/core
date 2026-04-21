package org.jboss.weld.tests.producer.disposer.dependent;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProducerDisposerDependentParameterTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProducerDisposerDependentParameterTest.class))
                .addPackage(ProducerDisposerDependentParameterTest.class.getPackage())
                .addClass(Utils.class);
    }

    @Test
    public void testDependentBeanCreatedAndDestroyedForProducerAndDisposer(BeanManager manager) {
        DependentBean.reset();

        Bean<Product> bean = Reflections.cast(manager.resolve(manager.getBeans(Product.class)));
        CreationalContext<Product> cc = manager.createCreationalContext(bean);
        Product instance = bean.create(cc);

        // after producer method invocation, one DependentBean should have been created
        assertEquals(1, DependentBean.getCreateCount());

        // destroy the produced instance, which triggers the disposer method
        bean.destroy(instance, cc);

        // producer and disposer each received their own DependentBean instance
        assertEquals(2, DependentBean.getCreateCount());
        // both dependent instances should have been destroyed
        assertEquals(2, DependentBean.getDestroyCount());
    }
}
