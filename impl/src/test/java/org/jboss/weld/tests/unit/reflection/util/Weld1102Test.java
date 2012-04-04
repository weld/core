package org.jboss.weld.tests.unit.reflection.util;

import junit.framework.Assert;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;

import javax.enterprise.util.TypeLiteral;

/**
 *
 */
public class Weld1102Test {

    @Test
    public <B extends Bar> void test1() throws Exception {
        Assert.assertTrue("Required type Foo<Bar<Integer>> should match bean type Foo<B extends Bar>",
            requiredTypeMatchesBeanType(
                new TypeLiteral<Foo<Bar<Integer>>>() {
                },
                new TypeLiteral<Foo<B>>() {
                }
            ));
    }

    @Test
    public <B extends Bar> void test2() throws Exception {
        Assert.assertTrue("Required type Foo<Bar<Baz>> should match bean type Foo<B extends Bar>",
            requiredTypeMatchesBeanType(
                new TypeLiteral<Foo<Bar<Baz>>>() {
                },
                new TypeLiteral<Foo<B>>() {
                }
            ));
    }

    @Test
    public <B extends Bar<Integer>> void test3() throws Exception {
        Assert.assertTrue("Required type Foo<Bar<Integer>>  should match bean type Foo<B extends Bar<Integer>>",
            requiredTypeMatchesBeanType(
                new TypeLiteral<Foo<Bar<Integer>>>() {
                },
                new TypeLiteral<Foo<B>>() {
                }
            ));
    }

    @Test
    public <B extends Bar<Integer>> void test4() throws Exception {
        Assert.assertTrue("Required type Foo<Bar<Number>> should match bean type Foo<B extends Bar<Integer>>",
            requiredTypeMatchesBeanType(
                new TypeLiteral<Foo<Bar<Number>>>() {
                },
                new TypeLiteral<Foo<B>>() {
                }
            ));
    }

    private boolean requiredTypeMatchesBeanType(TypeLiteral requiredType, TypeLiteral beanType) {
        return Reflections.matches(requiredType.getType(), beanType.getType());
    }
}
