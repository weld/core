package org.jboss.weld.tests.producer.method.weld994;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Instance;


/**
 * @author Marko Luksa
 */
@RunWith(Arquillian.class)
public class Weld994Test {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(Weld994Test.class.getPackage());
    }

    @Test
    public void testInjectUnserializableObjectIntoInstanceFieldOfPassivatingBean(PassivatingBean passivatingBean) {
        Instance<UnserializableObject> instance = passivatingBean.getUnserializableObjectInstance();
        instance.get(); // should not throw IllegalProductException
    }
}
