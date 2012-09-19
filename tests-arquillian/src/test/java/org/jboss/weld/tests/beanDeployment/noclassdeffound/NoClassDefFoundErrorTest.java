package org.jboss.weld.tests.beanDeployment.noclassdeffound;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(Arquillian.class)
public class NoClassDefFoundErrorTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(BeanArchive.class)
                .addClass(Bar.class);   // NOTE: no Foo on classpath
    }

    @Test
    public void testDeployment() {
    }
}
