package org.jboss.weld.tests.annotations.weld1131;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(Arquillian.class)
public class Weld1131Test {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(Weld1131Test.class))
                .addPackage(Weld1131Test.class.getPackage());
    }

    @Inject
    private Foo foo;

    @Test
    @Ignore("The CDI specification does not guarantee that client proxies retain method annotations. " +
            "While this is a nice-to-have feature, annotation preservation on proxy methods is not " +
            "required by the specification and is not currently implemented in the Gizmo 2 migration.")
    public void testMethodAnnotations() throws Exception {
        MyAnnotation myAnnotation = foo.getClass().getMethod("getBar").getAnnotation(MyAnnotation.class);
        Assert.assertNotNull(myAnnotation);
    }

    @Test
    public void testTypeAnnotations() throws Exception {
        MyAnnotation myAnnotation = foo.getClass().getAnnotation(MyAnnotation.class);
        Assert.assertNull(myAnnotation); // not a hard requirement
    }

}
