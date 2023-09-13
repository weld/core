package org.jboss.weld.tests.unit.reflection.util;

import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.resolution.AssignabilityRules;
import org.jboss.weld.resolution.BeanTypeAssignabilityRules;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author Jozef Hartinger
 */
@SuppressWarnings("serial")
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
        Assert.assertTrue("Foo<?>[] should be assignable from Foo<String>[]",
                getRules().matches(wildcardFooArrayType, stringFooArrayType));
    }

    @Test
    public void testStringFooArrayDoesNotMatchWildcardFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Type wildcardFooArrayType = new TypeLiteral<Foo<?>[]>() {
        }.getType();
        Assert.assertFalse("Foo<String>[] should not be assignable from Foo<?>[]",
                getRules().matches(stringFooArrayType, wildcardFooArrayType));
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
        Assert.assertTrue("Foo<Object> should be assignable to Foo",
                getRules().matches(Foo.class, new TypeLiteral<Foo<Object>>() {
                }.getType()));
    }

    @Test
    public <E> void testRawRequiredTypeMatchesParameterizedBeanWithUnboundedVariableTypeParameter() throws Exception {
        Assert.assertTrue("Foo<E> should be assignable to Foo", getRules().matches(Foo.class, new TypeLiteral<Foo<E>>() {
        }.getType()));
    }

    @Test
    public <F extends Number> void testParameterizedBeanWithBoundedVariableTypeParameter() throws Exception {
        Assert.assertFalse("Foo<F extends Number> should not be assignable to Foo",
                getRules().matches(Foo.class, new TypeLiteral<Foo<F>>() {
                }.getType()));
    }

    @Test
    public void testArrays() {
        Assert.assertTrue("int[][] should be assignable to int[][]",
                getRules().matches(new int[0][].getClass(), new int[0][].getClass()));
        Assert.assertTrue("Integer[][] should be assignable to Integer[][]",
                getRules().matches(new Integer[0][].getClass(), new Integer[0][].getClass()));
        Assert.assertTrue("List<Integer[][]> should be assignable to List<Integer[][]>",
                getRules().matches(new TypeLiteral<List<Integer[][]>>() {
                }.getType(), new TypeLiteral<List<Integer[][]>>() {
                }.getType()));
        Assert.assertFalse("List<Integer[][]> should not be assignable to List<Number[][]>",
                getRules().matches(new TypeLiteral<List<Number[][]>>() {
                }.getType(), new TypeLiteral<List<Integer[][]>>() {
                }.getType()));
    }

    @Test
    public void testArrayBoxing() {
        /*
         * This is not explicitly said in the CDI spec however Java SE does not support array boxing so neither should CDI.
         */
        Assert.assertFalse("Integer[] should not be assignable to int[]",
                getRules().matches(new int[0].getClass(), new Integer[0].getClass()));
        Assert.assertFalse("int[] should not be assignable to Integer[]",
                getRules().matches(new Integer[0].getClass(), new int[0].getClass()));
    }

    @Test
    public <T1 extends Number, T2 extends T1> void testTypeVariableWithTypeVariableBound() {
        Assert.assertTrue("List<T2 extends T1 extends Number> should be assignable to List<Number>",
                getRules().matches(new TypeLiteral<List<Number>>() {
                }.getType(), new TypeLiteral<List<T2>>() {
                }.getType()));
        Assert.assertFalse("List<T2 extends T1 extends Number> should not be assignable to List<Runnable>",
                getRules().matches(new TypeLiteral<List<Runnable>>() {
                }.getType(), new TypeLiteral<List<T2>>() {
                }.getType()));

        Assert.assertTrue("List<T2 extends T1 extends Number> should be assignable to List<T1 extends Number>",
                getRules().matches(new TypeLiteral<List<T1>>() {
                }.getType(), new TypeLiteral<List<T2>>() {
                }.getType()));
        Assert.assertTrue("List<T1 extends Number> should be assignable to List<T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<T2>>() {
                }.getType(), new TypeLiteral<List<T1>>() {
                }.getType()));
    }

    @Test
    public <T1 extends Exception, T2 extends T1, T3 extends Exception, T4 extends T3, T5 extends Throwable> void testTypeVariablesWithTypeVariableBounds() {
        Assert.assertTrue("List<T2 extends T1 extends Exception> should be assignable to List<T4 extends T3 extends Exception>",
                getRules().matches(new TypeLiteral<List<T4>>() {
                }.getType(), new TypeLiteral<List<T2>>() {
                }.getType()));
        Assert.assertTrue("List<T5 extends Throwable> should be assignable to List<T4 extends T3 extends Exception>",
                getRules().matches(new TypeLiteral<List<T4>>() {
                }.getType(), new TypeLiteral<List<T5>>() {
                }.getType()));
        Assert.assertFalse("List<T4 extends T3 extends Exception> should not be assignable to List<T5 extends Throwable>",
                getRules().matches(new TypeLiteral<List<T5>>() {
                }.getType(), new TypeLiteral<List<T4>>() {
                }.getType()));
    }

    @Test
    public <T1 extends Number, T2 extends T1> void testWildcardWithTypeVariableBound() {
        Assert.assertTrue("List<Number> should be assignable to List<? extends T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? extends T2>>() {
                }.getType(), new TypeLiteral<List<Number>>() {
                }.getType()));
        Assert.assertTrue("List<Integer> should be assignable to List<? extends T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? extends T2>>() {
                }.getType(), new TypeLiteral<List<Integer>>() {
                }.getType()));
        Assert.assertFalse("List<Object> should not be assignable to List<? extends T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? extends T2>>() {
                }.getType(), new TypeLiteral<List<Object>>() {
                }.getType()));

        Assert.assertTrue("List<Number> should be assignable to List<? super T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? super T2>>() {
                }.getType(), new TypeLiteral<List<Number>>() {
                }.getType()));
        Assert.assertFalse("List<Integer> should not be assignable to List<? super T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? super T2>>() {
                }.getType(), new TypeLiteral<List<Integer>>() {
                }.getType()));
        Assert.assertTrue("List<Object> should be assignable to List<? super T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? super T2>>() {
                }.getType(), new TypeLiteral<List<Object>>() {
                }.getType()));
    }

    @Test
    public <T1 extends List<?> & Appendable, T2 extends Writer & Serializable & Collection<?>, T3 extends T2, T4 extends Appendable & Iterable<?>, T5 extends T4> void testTypeVariableWithMultipleBounds() {
        Assert.assertTrue(
                "List<T4 extends Appendable & Iterable<?>> should be assignable to List<T1 extends List<?> & Appendable>",
                getRules().matches(new TypeLiteral<List<T1>>() {
                }.getType(), new TypeLiteral<List<T4>>() {
                }.getType()));
        Assert.assertTrue(
                "List<T4 extends Appendable & Iterable<?>> should be assignable to List<T2 extends Writer & Serializable & Collection<?>>",
                getRules().matches(new TypeLiteral<List<T2>>() {
                }.getType(), new TypeLiteral<List<T4>>() {
                }.getType()));
        Assert.assertTrue(
                "List<T5 extends T4 extends Appendable & Iterable<?>> should be assignable to List<T3 extends T2 extends Writer & Serializable & Collection<?>>",
                getRules().matches(new TypeLiteral<List<T3>>() {
                }.getType(), new TypeLiteral<List<T5>>() {
                }.getType()));
        // copy & paste with wildcard
        Assert.assertTrue(
                "List<T4 extends Appendable & Iterable<?>> should be assignable to List<? extends T1 extends List<?> & Appendable>",
                getRules().matches(new TypeLiteral<List<? extends T1>>() {
                }.getType(), new TypeLiteral<List<T4>>() {
                }.getType()));
        Assert.assertTrue(
                "List<T4 extends Appendable & Iterable<?>> should be assignable to List<? extends T2 extends Writer & Serializable & Collection<?>>",
                getRules().matches(new TypeLiteral<List<? extends T2>>() {
                }.getType(), new TypeLiteral<List<T4>>() {
                }.getType()));
        Assert.assertTrue(
                "List<T5 extends T4 extends Appendable & Iterable<?>> should be assignable to List<? extends T3 extends T2 extends Writer & Serializable & List<?>>",
                getRules().matches(new TypeLiteral<List<? extends T3>>() {
                }.getType(), new TypeLiteral<List<T5>>() {
                }.getType()));

        Assert.assertFalse(
                "List<T1 extends List<?> & Appendable> should not be assignable to List<T4 extends Appendable & Iterable<?>>",
                getRules().matches(new TypeLiteral<List<T4>>() {
                }.getType(), new TypeLiteral<List<T1>>() {
                }.getType()));
        Assert.assertFalse(
                "List<T1 extends List<?> & Appendable> should not be assignable to List<T2 extends Writer & Serializable & Collection<?>>",
                getRules().matches(new TypeLiteral<List<T2>>() {
                }.getType(), new TypeLiteral<List<T1>>() {
                }.getType()));
        Assert.assertFalse(
                "List<T2 extends Writer & Serializable & Collection<?>> should not be assignable to List<T4 extends Appendable & Iterable<?>>",
                getRules().matches(new TypeLiteral<List<T4>>() {
                }.getType(), new TypeLiteral<List<T2>>() {
                }.getType()));

        // neither one type variable has stricter bounds than the other
        Assert.assertFalse(
                "List<T2 extends Writer & Serializable & Collection<?>> should not be assignable to List<T1 extends List<?> & Appendable>",
                getRules().matches(new TypeLiteral<List<T1>>() {
                }.getType(), new TypeLiteral<List<T2>>() {
                }.getType()));
        Assert.assertFalse(
                "List<T1 extends List<?> & Appendable> should not be assignable to List<T2 extends Writer & Serializable & Collection<?>>",
                getRules().matches(new TypeLiteral<List<T2>>() {
                }.getType(), new TypeLiteral<List<T1>>() {
                }.getType()));
        // copy & paste with wildcard
        Assert.assertFalse(
                "List<T2 extends Writer & Serializable & Collection<?>> should not be assignable to List<? extends T1 extends List<?> & Appendable>",
                getRules().matches(new TypeLiteral<List<? extends T1>>() {
                }.getType(), new TypeLiteral<List<T2>>() {
                }.getType()));
        Assert.assertFalse(
                "List<T1 extends List<?> & Appendable> should not be assignable to List<? extends T2 extends Writer & Serializable & Collection<?>>",
                getRules().matches(new TypeLiteral<List<? extends T2>>() {
                }.getType(), new TypeLiteral<List<T1>>() {
                }.getType()));
    }

    // test that java assignability rules are used to compare parameters of parameterized types
    @Test
    public <T1, T2 extends T1, T3 extends Collection<T1>, T4 extends Collection<T2>, T5 extends Collection<Number>, T6 extends Collection<Integer>, T7 extends Collection<?>> void testTypeVariableWithParameterizedTypesAsBounds() {
        Assert.assertTrue("List<T5 extends Collection<Number>> should be assignable to itself",
                getRules().matches(new TypeLiteral<List<T5>>() {
                }.getType(), new TypeLiteral<List<T5>>() {
                }.getType()));
        Assert.assertFalse(
                "List<T5 extends Collection<Number>> should not be assignable to List<T6 extends Collection<Integer>>",
                getRules().matches(new TypeLiteral<List<T6>>() {
                }.getType(), new TypeLiteral<List<T5>>() {
                }.getType()));
        Assert.assertFalse(
                "List<T6 extends Collection<Integer>> should not be assignable to List<T5 extends Collection<Number>>",
                getRules().matches(new TypeLiteral<List<T5>>() {
                }.getType(), new TypeLiteral<List<T6>>() {
                }.getType()));
        Assert.assertFalse("List<T5 extends Collection<Number>> should not be assignable to List<T7 extends Collection<?>>",
                getRules().matches(new TypeLiteral<List<T7>>() {
                }.getType(), new TypeLiteral<List<T5>>() {
                }.getType()));

        Assert.assertTrue("List<T3 extends Collection<T1>> should be assignable to itself",
                getRules().matches(new TypeLiteral<List<T3>>() {
                }.getType(), new TypeLiteral<List<T3>>() {
                }.getType()));
        Assert.assertFalse(
                "List<T3 extends Collection<T1>> should not be assignable to List<T4 extends Collection<T2 extends T1>>",
                getRules().matches(new TypeLiteral<List<T4>>() {
                }.getType(), new TypeLiteral<List<T3>>() {
                }.getType()));
        Assert.assertFalse(
                "List<T4 extends Collection<T2 extends T1>> should not be assignable to List<T3 extends Collection<T1>>",
                getRules().matches(new TypeLiteral<List<T3>>() {
                }.getType(), new TypeLiteral<List<T4>>() {
                }.getType()));
        Assert.assertFalse("List<T3 extends Collection<T1>> should not be assignable to List<T7 extends Collection<?>>",
                getRules().matches(new TypeLiteral<List<T7>>() {
                }.getType(), new TypeLiteral<List<T3>>() {
                }.getType()));
    }
}
