package org.jboss.weld.tests.unit.reflection.util;

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.resolution.BeanTypeAssignabilityRules;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

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
                        }));
    }

    @Test
    public <B extends Bar> void test2() throws Exception {
        Assert.assertTrue("Required type Foo<Bar<Baz>> should match bean type Foo<B extends Bar>",
                requiredTypeMatchesBeanType(
                        new TypeLiteral<Foo<Bar<Baz>>>() {
                        },
                        new TypeLiteral<Foo<B>>() {
                        }));
    }

    @Test
    public <B extends Bar<Integer>> void test3() throws Exception {
        Assert.assertTrue("Required type Foo<Bar<Integer>>  should match bean type Foo<B extends Bar<Integer>>",
                requiredTypeMatchesBeanType(
                        new TypeLiteral<Foo<Bar<Integer>>>() {
                        },
                        new TypeLiteral<Foo<B>>() {
                        }));
    }

    @Test
    @Ignore // this test seems broken since Bar<Number> is not assignable from B
    public <B extends Bar<Integer>> void test4() throws Exception {
        Assert.assertTrue("Required type Foo<Bar<Number>> should match bean type Foo<B extends Bar<Integer>>",
                requiredTypeMatchesBeanType(
                        new TypeLiteral<Foo<Bar<Number>>>() {
                        },
                        new TypeLiteral<Foo<B>>() {
                        }));
    }

    @Test
    public <B extends Bar<Integer>, C extends B> void test5() throws Exception {
        Assert.assertTrue("Required type Foo<Bar<Integer>>  should match bean type Foo<C extends B extends Bar<Integer>>",
                requiredTypeMatchesBeanType(
                        new TypeLiteral<Foo<Bar<Integer>>>() {
                        },
                        new TypeLiteral<Foo<C>>() {
                        }));
    }

    private boolean requiredTypeMatchesBeanType(TypeLiteral requiredType, TypeLiteral beanType) {
        return BeanTypeAssignabilityRules.instance().matches(requiredType.getType(), beanType.getType());
    }
}
