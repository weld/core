package org.jboss.weld.tests.resolution;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Iterator;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class InstanceTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(InstanceTest.class.getPackage());
    }

    @Inject
    private Instance<Object> instance;

    @Test
    public void testSelect() throws Exception {
        Iterator<Object> iter = instance.select().iterator();
        while (iter.hasNext()) {
            assertNotNull(iter.next());
        }
    }

}
