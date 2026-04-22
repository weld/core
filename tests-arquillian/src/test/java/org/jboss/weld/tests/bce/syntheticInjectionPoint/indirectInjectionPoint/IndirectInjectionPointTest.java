package org.jboss.weld.tests.bce.syntheticInjectionPoint.indirectInjectionPoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.spi.BeanContainer;
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
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        IndirectInjectionPointExtension.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testIndirectInjectionPointMetadata() {
        Instance<Object> lookup = container.createInstance();
        Instance.Handle<SyntheticPojo> handle = lookup.select(SyntheticPojo.class).getHandle();
        SyntheticPojo pojo = handle.get();

        assertNotNull(pojo.capturedInjectionPoint);
        assertEquals(InjectionPointCaptor.class, pojo.capturedInjectionPoint.getType());
        assertTrue(pojo.capturedInjectionPoint.getQualifiers().contains(Default.Literal.INSTANCE));
        assertNotNull(pojo.capturedInjectionPoint.getBean());
        assertEquals(SyntheticPojo.class, pojo.capturedInjectionPoint.getBean().getBeanClass());
        assertFalse(pojo.capturedInjectionPoint.isDelegate());
        assertFalse(pojo.capturedInjectionPoint.isTransient());
        assertNull(pojo.capturedInjectionPoint.getAnnotated());
        assertNull(pojo.capturedInjectionPoint.getMember());

        handle.destroy();
    }
}
