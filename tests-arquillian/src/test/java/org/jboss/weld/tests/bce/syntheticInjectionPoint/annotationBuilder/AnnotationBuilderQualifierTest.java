package org.jboss.weld.tests.bce.syntheticInjectionPoint.annotationBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
public class AnnotationBuilderQualifierTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(AnnotationBuilderQualifierTest.class))
                .addPackage(AnnotationBuilderQualifierTest.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        AnnotationBuilderQualifierExtension.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testAnnotationBuilderQualifierMatchesLiteral() {
        Instance<Object> lookup = container.createInstance();
        Instance.Handle<SyntheticPojo> handle = lookup.select(SyntheticPojo.class).getHandle();
        SyntheticPojo pojo = handle.get();
        assertNotNull(pojo);
        assertEquals("plain", pojo.plainName);
        assertEquals("special", pojo.specialName);
        assertEquals("tagged-foo", pojo.taggedName);
        handle.destroy();
    }
}
