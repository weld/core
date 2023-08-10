package org.jboss.weld.tests.invokable.metadata.buildCompatibleExtension;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.invokable.metadata.common.UnannotatedBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests correct detection of invokable methods via BCE
 */
@RunWith(Arquillian.class)
public class InvokableMethodDetectionBCETest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InvokableMethodDetectionBCETest.class))
                .addPackage(InvokableMethodDetectionBCETest.class.getPackage())
                .addPackage(UnannotatedBean.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class, BuildExtension.class);
    }


    @Test
    public void testAnnotationDetected() {
        // assertions are made through BuildExtension.class
        // here we only verify that the extension methods were invoked as expected
        Assert.assertEquals(9, BuildExtension.timesInvoked);
    }
}
