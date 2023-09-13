package org.jboss.weld.tests.assignability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@SuppressWarnings("serial")
@RunWith(Arquillian.class)
public class AssignabilityTest {

    @Inject
    private BeanManager beanManager;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AssignabilityTest.class))
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
        Assert.assertEquals(1, beans.size());
    }

    @Test
    public void testAssignability6() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Dao<? extends User>>() {
        }.getType());
        Assert.assertEquals(2, beans.size());
    }

    @Test
    public void testAssignability7() {
        Set<Bean<?>> beans = beanManager.getBeans(Dao.class);
        Assert.assertEquals(0, beans.size());
    }

    /*
     * A raw bean type is considered assignable to a parameterized required type if the raw types are identical and all type
     * parameters of the required type are either unbounded type variables or java.lang.Object.
     */

    @Test
    public void testAssignability8() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Animal<Object, Object, Object>>() {
        }.getType());
        assertEquals(1, beans.size());
        assertTrue(beans.iterator().next().getName().equals("zebra"));
    }

    @Test
    public <T> void testAssignability9() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Animal<Object, T, Object>>() {
        }.getType());
        assertEquals(1, beans.size());
        assertTrue(beans.iterator().next().getName().equals("zebra"));
    }

    @Test
    public <T extends Number> void testAssignability10() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Animal<Object, T, Object>>() {
        }.getType());
        assertEquals(0, beans.size());
    }

    @Test
    public void testAssignability11() {
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<Animal<Object, Object, String>>() {
        }.getType());
        assertEquals(0, beans.size());
    }

    @Test
    public void testAssignability12() {
        Set<Bean<?>> beans = beanManager.getBeans(Animal.class);
        assertEquals(1, beans.size());
        assertTrue(beans.iterator().next().getName().equals("zebra"));
    }
}
