package org.jboss.weld.tests.annotatedType.superclass;

import static org.junit.Assert.assertNotNull;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Gert Palok
 * @author Ales Justin
 */
@RunWith(Arquillian.class)
public class SuperclassTest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SuperclassTest.class))
                .addPackage(Child.class.getPackage())
                .addAsServiceProvider(Extension.class, TestExtension.class);
    }

    @Test
    public void shouldInjectSuperclassFields(Child child) {
        assertNotNull("Should resolve Child", child);
        assertNotNull("Should have Foo injected", child.getFoo());
    }
}
