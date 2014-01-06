package org.jboss.weld.tests.proxy.superclass;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SuperclassWithConstructorWithArgumentsTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(SuperclassWithConstructorWithArgumentsTest.class.getPackage());
    }

    @Inject
    private SimpleBean bean;

    @Test
    public void testSuperClassWithoutSimpleConstructor() {
    }
}
