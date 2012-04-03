package org.jboss.weld.tests.assignability;

import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import java.util.Set;

/**
 *
 */
@RunWith(Arquillian.class)
public class AssignabilityTest {

    @Inject
    private BeanManager beanManager;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
            .addPackage(AssignabilityTest.class.getPackage());
    }


    @Test
    public void testAssignability1() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Dao<Order>>() {
        }.getType());

        Assert.assertEquals(1, beans.size());
    }

    @Test
    public void testAssignability2() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Dao<User>>() {
        }.getType());
        Assert.assertEquals(2, beans.size());
    }

    @Test
    public void testAssignability3() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Dao<?>>() {
        }.getType());
        System.err.println("beans = " + beans);
        Assert.assertEquals(2, beans.size());
    }

    @Test
    public void testAssignability4() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Dao<? extends Persistent>>() {
        }.getType());
        Assert.assertEquals(2, beans.size());
    }

    @Test
    public <X extends Persistent> void testAssignability5() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Dao<X>>() {
        }.getType());
        Assert.assertEquals(2, beans.size());
    }

    @Test
    public void testAssignability6() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Dao<? extends User>>() {
        }.getType());
        Assert.assertEquals(2, beans.size());
    }

    @Ignore("WELD-1054")
    @Test
    public void testAssignability7() {
        Set<Bean<?>> beans = beanManager.getBeans(Dao.class);
        Assert.assertEquals(0, beans.size());
    }

}
