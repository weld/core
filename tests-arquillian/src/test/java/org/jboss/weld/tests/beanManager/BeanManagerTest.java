package org.jboss.weld.tests.beanManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanManagerTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(BeanManagerTest.class))
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

    @Test
    public void testResolveWithNull() {
        assertNull(beanManager.resolve(null));
    }

    @Test
    public void testResolveWithEmptySet() {
        assertNull(beanManager.resolve(Collections.<Bean<? extends Integer>> emptySet()));
        assertNull(beanManager.resolve(new HashSet<Bean<? extends Integer>>()));
    }
}
