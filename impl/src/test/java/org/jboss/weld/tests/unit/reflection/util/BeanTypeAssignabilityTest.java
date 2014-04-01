package org.jboss.weld.tests.unit.reflection.util;

import java.lang.reflect.Type;

import javax.enterprise.util.TypeLiteral;

import junit.framework.Assert;

import org.jboss.weld.resolution.AssignabilityRules;
import org.jboss.weld.resolution.BeanTypeAssignabilityRules;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class BeanTypeAssignabilityTest {

    protected AssignabilityRules getRules() {
        return BeanTypeAssignabilityRules.instance();
    }

    @Test
    public void testGetRawType() throws Exception {
        Assert.assertEquals(Foo.class, Reflections.getRawType(new TypeLiteral<Foo<Integer>>() {
        }.getType()));
    }

    @Test
    public void testGetRawTypeOfArray() throws Exception {
        Assert.assertEquals(Foo[].class, Reflections.getRawType(new TypeLiteral<Foo<Integer>[]>() {
        }.getType()));
    }

    @Test
    public <E> void testTypeVariableMatchesItself() throws Exception {
        Type type = new TypeLiteral<E>() {
        }.getType();
        Assert.assertTrue("E should match itself", getRules().matches(type, type));
    }

    @Test
    public <E> void testTypeVariableArrayMatchesItself() throws Exception {
        Type type = new TypeLiteral<E[]>() {
        }.getType();
        Assert.assertTrue("E[] should match itself", getRules().matches(type, type));
    }

    @Test
    public void testIntegerFooMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<Integer>>() {
        }.getType();
        Assert.assertTrue("type should match itself", getRules().matches(type, type));
    }

    @Test
    public void testIntegerFooDoesNotMatchStringFoo() throws Exception {
        Type type1 = new TypeLiteral<Foo<Integer>>() {
        }.getType();
        Type type2 = new TypeLiteral<Foo<String>>() {
        }.getType();
        Assert.assertFalse("Foo<Integer> should not match Foo<String>", getRules().matches(type1, type2));
    }

    @Test
    public void testFooMatchesItself() throws Exception {
        Type type = Foo.class;
        Assert.assertTrue("type should match itself", getRules().matches(type, type));
    }

    @Test
    public void testParameterizedArrayDoesNotMatchComponentOfArray() throws Exception {
        Type arrayType = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Type componentType = new TypeLiteral<Foo<String>>() {
        }.getType();
        Assert.assertFalse("array type should not match its component type", getRules().matches(arrayType, componentType));
    }

    @Test
    public void testParameterizedArrayMatches() throws Exception {
        Type type = new TypeLiteral<Foo<Integer>[]>() {
        }.getType();
        Assert.assertTrue("type should match itself", getRules().matches(type, type));
    }

    @Test
    public void testArraysDontMatch() throws Exception {
        Type type1 = new TypeLiteral<Foo<Integer>[]>() {
        }.getType();
        Type type2 = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Assert.assertFalse("Foo<Integer>[] should not match Foo<String>[]", getRules().matches(type1, type2));
    }

    @Test
    public void testWildcardFooMatchesStringFoo() throws Exception {
        Type stringFooType = new TypeLiteral<Foo<String>>() {
        }.getType();
        Type wildcardFooType = new TypeLiteral<Foo<?>>() {
        }.getType();
        Assert.assertTrue("Foo<?> should be assignable from Foo<String>", getRules().matches(wildcardFooType, stringFooType));
    }

    @Test
    public void testWildcardFooArrayMatchesStringFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Type wildcardFooArrayType = new TypeLiteral<Foo<?>[]>() {
        }.getType();
        Assert.assertFalse("Foo<?>[] should not be assignable from Foo<String>[]", getRules().matches(wildcardFooArrayType, stringFooArrayType));
    }

    @Test
    public void testStringFooArrayDoesNotMatchWildcardFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Type wildcardFooArrayType = new TypeLiteral<Foo<?>[]>() {
        }.getType();
        Assert.assertFalse("Foo<String>[] should not be assignable from Foo<?>[]", getRules().matches(stringFooArrayType, wildcardFooArrayType));
    }

    @Test
    public <E> void testVariableFooMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<E>>() {
        }.getType();
        Assert.assertTrue("Foo<E> should be assignable from Foo<E>", getRules().matches(type, type));
    }

    @Test
    public <E> void testStringFooMatchesVariableFoo() throws Exception {
        Type stringFooType = new TypeLiteral<Foo<String>>() {
        }.getType();
        Type variableFooType = new TypeLiteral<Foo<E>>() {
        }.getType();
        Assert.assertTrue("Foo<String> should match Foo<E>", getRules().matches(stringFooType, variableFooType));
    }

    @Test
    public <E> void testVariableFooArrayMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<E>[]>() {
        }.getType();
        Assert.assertTrue("Foo<E>[] should be assignable from Foo<E>[]", getRules().matches(type, type));
    }

    @Test
    public void testRawRequiredTypeMatchesParameterizedBeanWithObjectTypeParameter() throws Exception {
        Assert.assertTrue("Foo<Object> should be assignable to Foo", getRules().matches(Foo.class, new TypeLiteral<Foo<Object>>() {
        }.getType()));
    }

    @Test
    public <E> void testRawRequiredTypeMatchesParameterizedBeanWithUnboundedVariableTypeParameter() throws Exception {
        Assert.assertTrue("Foo<E> should be assignable to Foo", getRules().matches(Foo.class, new TypeLiteral<Foo<E>>() {
        }.getType()));
    }

    @Test
    public <F extends Number> void testParameterizedBeanWithBoundedVariableTypeParameter() throws Exception {
        Assert.assertFalse("Foo<F extends Number> should not be assignable to Foo", getRules().matches(Foo.class, new TypeLiteral<Foo<F>>() {
        }.getType()));
    }

}