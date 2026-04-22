package org.jboss.weld.tests.injectionPoint.beanConfigurator.indirect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.Extension;
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
public class IndirectInjectionPointTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(IndirectInjectionPointTest.class))
                .addPackage(IndirectInjectionPointTest.class.getPackage())
                .addAsServiceProvider(Extension.class,
                        IndirectInjectionPointExtension.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testDependentBeanSeesProducingBeanInInjectionPoint() {
        Instance<Object> lookup = container.createInstance();
        Instance.Handle<InjectionPointResult> handle = lookup.select(InjectionPointResult.class).getHandle();
        InjectionPointResult result = handle.get();

        assertNotNull(result.injectionPoint);
        assertEquals(InjectionPointCaptor.class, result.injectionPoint.getType());
        assertNotNull(result.injectionPoint.getBean());
        assertEquals(InjectionPointResult.class, result.injectionPoint.getBean().getBeanClass());
        assertFalse(result.injectionPoint.isDelegate());
        assertFalse(result.injectionPoint.isTransient());
        assertNull(result.injectionPoint.getAnnotated());
        assertNull(result.injectionPoint.getMember());

        handle.destroy();
    }
}
