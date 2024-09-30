package org.jboss.weld.tests.injectionPoint.resource.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test inheritance of {@code @Resource} field when adding a specialized bean through discovery versus through extension
 * See https://issues.redhat.com/browse/WELD-2798
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class SpecializationExtensionTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(SpecializationExtensionTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(Foo.class, FooSpecializedNoBeanDef.class, MyExtension.class,
                        SpecializationExtensionTest.class)
                .addAsServiceProvider(Extension.class, MyExtension.class)
                .addAsWebInfResource(SpecializationExtensionTest.class.getPackage(), "web.xml", "web.xml")
                // archive has an extension, it also needs beans.xml to be considered bean archive
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

    }

    @Inject
    Foo foo;

    @Test
    public void test() {
        // foo is an instance of the specialized bean
        assertTrue(foo instanceof FooSpecializedNoBeanDef);
        // resource has been injected
        assertEquals("hello world", foo.value());
    }
}
