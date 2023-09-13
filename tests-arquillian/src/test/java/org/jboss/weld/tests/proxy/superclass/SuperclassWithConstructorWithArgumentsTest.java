package org.jboss.weld.tests.proxy.superclass;

import static org.junit.Assert.assertEquals;

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
public class SuperclassWithConstructorWithArgumentsTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(SuperclassWithConstructorWithArgumentsTest.class))
                .addPackage(SuperclassWithConstructorWithArgumentsTest.class.getPackage());
    }

    @Inject
    private SimpleBean bean;

    @Test
    public void testSuperClassWithoutSimpleConstructor() {
        // tests deployability of this scenario
        // SimpleBean superclass has a constructor with args
        assertEquals("nothing", bean.giveMeNothing());
    }
}
