package org.jboss.weld.tests.invokable.metadata;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that methods which are supposed to be invokable are detected correctly.
 * This includes declaring {@link jakarta.enterprise.invoke.Invokable} directly, via another annotation and
 * through extension.
 */
@RunWith(Arquillian.class)
public class InvokableMethodDetectionTest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InvokableMethodDetectionTest.class))
                .addPackage(InvokableMethodDetectionTest.class.getPackage())
                .addAsServiceProvider(Extension.class, ObservingExtension.class);
    }

    @Inject
    ObservingExtension extension;

    @Test
    public void testAnnotationDetected() {
        // test classic declaration
        Assert.assertEquals(2, extension.getClassDirectMethods().size());
        Assert.assertEquals(2, extension.getClassIndirectMethods().size());
        Assert.assertEquals(1, extension.getMethodDirectMethods().size());
        Assert.assertEquals(1, extension.getMethodIndirectMethods().size());

        // test marking annotation as @Invokable and then detecting methods
        Assert.assertEquals(2, extension.getClassExtensionMethods().size());
        Assert.assertEquals(1, extension.getMethodExtensionMethods().size());

        // test using extension to programatically add annotation which already is @Invokable
        Assert.assertEquals(2, extension.getUnannotatedBeanMethods().size());

        // TODO also add test with BCE

    }

    // TODO test for hierarchies once we know if the annotation is inherited and how what should behave
}
