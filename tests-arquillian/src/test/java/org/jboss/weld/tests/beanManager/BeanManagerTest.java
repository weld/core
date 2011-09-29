package org.jboss.weld.tests.beanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class BeanManagerTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(BeanManagerTest.class.getPackage())
                .addClass(Utils.class);
    }

    @Inject
    private BeanManagerImpl beanManager;

    @Test(expected = IllegalArgumentException.class)
    public void testNullBeanArgumentToGetReference() {
        Bean<Foo> bean = Utils.getBean(beanManager, Foo.class);
        CreationalContext<Foo> cc = beanManager.createCreationalContext(bean);
        beanManager.getReference(null, Foo.class, cc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullBeanTypeArgumentToGetReference() {
        Bean<Foo> bean = Utils.getBean(beanManager, Foo.class);
        CreationalContext<Foo> cc = beanManager.createCreationalContext(bean);
        beanManager.getReference(bean, null, cc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCreationalContextArgumentToGetReference() {
        Bean<Foo> bean = Utils.getBean(beanManager, Foo.class);
        beanManager.getReference(bean, Foo.class, null);
    }

    @Test
    // WELD-576
    public void testObjectIsValidTypeForGetReference() {
        Set<Bean<?>> beans = beanManager.getBeans("myBean");
        Bean<?> sourceBean = beans.iterator().next();
        Object myBean = beanManager.getReference(sourceBean, Object.class, beanManager.createCreationalContext(sourceBean));
        assertTrue(myBean instanceof UserInfo);
        assertEquals(((UserInfo) myBean).getUsername(), "pmuir");
    }
}
