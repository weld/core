package org.jboss.weld.tests.beanDeployment.noclassdeffound;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for WELD-1669.
 */
@RunWith(Arquillian.class)
public class MissingTypeArgumentTest {

    @Deployment(testable = false)
    public static JavaArchive deploy() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(FooBarProducer.class, GenericBar.class); // no Foo.class
    }

    @Test
    public void test() {
    }

}
