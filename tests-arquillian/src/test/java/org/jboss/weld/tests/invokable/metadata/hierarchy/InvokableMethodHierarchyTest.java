package org.jboss.weld.tests.invokable.metadata.hierarchy;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
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

@RunWith(Arquillian.class)
public class InvokableMethodHierarchyTest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InvokableMethodHierarchyTest.class))
                .addPackage(InvokableMethodHierarchyTest.class.getPackage())
                .addAsServiceProvider(Extension.class, ObservingExtension.class);
    }

    @Inject
    ObservingExtension extension;

    @Test
    public void testInvokableMethodDiscovery() {
        Assert.assertEquals(2, extension.getChildInvokableMethod().size());
        for (AnnotatedMethod<? super Child> method : extension.getChildInvokableMethod()) {
            String methodName = method.getJavaMember().getName();
            Assert.assertTrue(extension.getChildInvokableMethod().toString(), methodName.equals("child") || methodName.equals("commonAncestor"));
        }
    }
}
