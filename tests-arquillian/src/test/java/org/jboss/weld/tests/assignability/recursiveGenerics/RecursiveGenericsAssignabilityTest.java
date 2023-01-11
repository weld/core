package org.jboss.weld.tests.assignability.recursiveGenerics;

import static org.junit.Assert.assertEquals;

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
import org.jboss.weld.tests.assignability.recursiveGenerics.DuplicateRecursion.FooBar;
import org.jboss.weld.tests.assignability.recursiveGenerics.DuplicateRecursion.FooBarImpl;
import org.jboss.weld.tests.assignability.recursiveGenerics.MutualRecursion.City;
import org.jboss.weld.tests.assignability.recursiveGenerics.MutualRecursion.Graph;
import org.jboss.weld.tests.assignability.recursiveGenerics.MutualRecursion.Map;
import org.jboss.weld.tests.assignability.recursiveGenerics.MutualRecursion.Route;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
@RunWith(Arquillian.class)
public class RecursiveGenericsAssignabilityTest {

    @Inject
    private BeanManager beanManager;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(RecursiveGenericsAssignabilityTest.class))
            .addPackage(RecursiveGenericsAssignabilityTest.class.getPackage());
    }

    @Test
    public void testResursiveGenericAssignability() {
        // @Inject List<String> should work
        Set<Bean<?>> beans = beanManager.getBeans(new TypeLiteral<List<String>>(){}.getType());
        assertEquals(1, beans.size());

        // @Inject List<Object> should NOT work, Object is not Comparable
        beans = beanManager.getBeans(new TypeLiteral<List<Object>>(){}.getType());
        assertEquals(0, beans.size());

        // @Inject FooBar<FooBarImpl, String>> should work
        beans = beanManager.getBeans(new TypeLiteral<FooBar<FooBarImpl, String>>(){}.getType());
        assertEquals(1, beans.size());

        // @Inject Graph<Map, Route, City>> should work
        beans = beanManager.getBeans(new TypeLiteral<Graph<Map, Route, City>>(){}.getType());
        assertEquals(1, beans.size());
    }
}
