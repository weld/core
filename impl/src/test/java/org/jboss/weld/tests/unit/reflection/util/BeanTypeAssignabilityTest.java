package org.jboss.weld.tests.unit.reflection.util;

import java.lang.reflect.Type;
import java.util.List;

import javax.enterprise.util.TypeLiteral;

import junit.framework.Assert;

import org.jboss.weld.resolution.AssignabilityRules;
import org.jboss.weld.resolution.BeanTypeAssignabilityRules2;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author Jozef Hartinger
 */
@SuppressWarnings("serial")
public class BeanTypeAssignabilityTest {

    protected AssignabilityRules getRules() {
        return BeanTypeAssignabilityRules2.instance();
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
        Assert.assertTrue("Foo<?>[] should be assignable from Foo<String>[]", getRules().matches(wildcardFooArrayType, stringFooArrayType));
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

    @Test
    public void testArrays() {
        Assert.assertTrue("int[][] should be assignable to int[][]", getRules().matches(new int[0][].getClass(), new int[0][].getClass()));
        Assert.assertTrue("Integer[][] should be assignable to Integer[][]", getRules().matches(new Integer[0][].getClass(), new Integer[0][].getClass()));
        Assert.assertTrue("List<Integer[][]> should be assignable to List<Integer[][]>", getRules().matches(new TypeLiteral<List<Integer[][]>>() {}.getType(), new TypeLiteral<List<Integer[][]>>() {}.getType()));
        Assert.assertFalse("List<Integer[][]> should not be assignable to List<Number[][]>", getRules().matches(new TypeLiteral<List<Number[][]>>() {}.getType(), new TypeLiteral<List<Integer[][]>>() {}.getType()));
    }

    @Test
    public void testArrayBoxing() {
        /*
         * This is not explicitly said in the CDI spec however Java SE does not support array boxing so neither should CDI.
         */
        Assert.assertFalse("Integer[] should not be assignable to int[]", getRules().matches(new int[0].getClass(), new Integer[0].getClass()));
        Assert.assertFalse("int[] should not be assignable to Integer[]", getRules().matches(new Integer[0].getClass(), new int[0].getClass()));
    }

    @Test
    public <T1 extends Number, T2 extends T1> void testTypeVariableWithTypeVariableBound() {
        Assert.assertTrue("List<T2 extends T1 extends Number> should be assignable to List<Number>", getRules().matches(new TypeLiteral<List<Number>>() {}.getType(), new TypeLiteral<List<T2>>() {}.getType()));
        Assert.assertFalse("List<T2 extends T1 extends Number> should not be assignable to List<Runnable>", getRules().matches(new TypeLiteral<List<Runnable>>() {}.getType(), new TypeLiteral<List<T2>>() {}.getType()));
    }

}