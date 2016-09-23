package org.jboss.weld.tests.ejb.singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class SingletonStartupTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(SingletonStartupTest.class.getPackage());
    }

    @Test
    public void testSingletonStartup() {
        assert Foo.isPostConstructCalled();
    }

    @Test
    public void testSingletonStartupCount(Foo foo) {
        int count = 10;
        for (int i = 0; foo.getSomeValue() && i < count; i++) {
        }
        assertEquals(1, Foo.getCountOfPostConstructCalled());
        // Only test that constructor is not called per method invocation
        assertTrue(Foo.getCountOfConstructorCalled() < count);
    }
}
