package org.jboss.weld.tests.unit.reflection.util;

import junit.framework.Assert;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Ignore;
import org.junit.Test;

import javax.enterprise.util.TypeLiteral;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ReflectionsTest<E> {

    @Test
    public void testGetRawType() throws Exception {
        Assert.assertEquals(Foo.class, Reflections.getRawType(new TypeLiteral<Foo<Integer>>(){}.getType()));
    }

    @Test
    public void testGetRawTypeOfArray() throws Exception {
        Assert.assertEquals(Foo[].class, Reflections.getRawType(new TypeLiteral<Foo<Integer>[]>(){}.getType()));
    }



    @Test
    public void testTypeVariableMatchesItself() throws Exception {
        Type type = new TypeLiteral<E>(){}.getType();
        Assert.assertTrue("E should match itself", Reflections.matches(type, type));
    }

    @Ignore
    @Test
    public void testTypeVariableArrayMatchesItself() throws Exception {
        Type type = new TypeLiteral<E[]>(){}.getType();
        Assert.assertTrue("E[] should match itself", Reflections.matches(type, type));
    }

    @Test
    public void testTypeVariableMatchesFoo() throws Exception {
        Type fooType = Foo.class;
        Type variableType = new TypeLiteral<E>(){}.getType();
        Assert.assertTrue("E should be assignable from Foo", Reflections.matches(variableType, fooType));
    }

    @Test
    public void testIntegerFooMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<Integer>>(){}.getType();
        Assert.assertTrue("type should match itself", Reflections.matches(type, type));
    }

    @Test
    public void testIntegerFooDoesNotMatchStringFoo() throws Exception {
        Type type1 = new TypeLiteral<Foo<Integer>>(){}.getType();
        Type type2 = new TypeLiteral<Foo<String>>(){}.getType();
        Assert.assertFalse("Foo<Integer> should not match Foo<String>", Reflections.matches(type1, type2));
    }

    @Test
    public void testFooMatchesItself() throws Exception {
        Type type = Foo.class;
        Assert.assertTrue("type should match itself", Reflections.matches(type, type));
    }

    @Test
    public void testFooArrayMatchesItself() throws Exception {
        Type clazz = Foo[].class;
        Type genericArrayType = new TypeLiteral<Foo[]>(){}.getType();
        Assert.assertTrue("array should match itself", Reflections.matches(clazz, clazz));
        Assert.assertTrue("array should match itself", Reflections.matches(genericArrayType, genericArrayType));
        Assert.assertTrue("array should match itself", Reflections.matches(genericArrayType, clazz));
        Assert.assertTrue("array should match itself", Reflections.matches(clazz, genericArrayType));
    }

    @Test
    public void testParameterizedArrayDoesNotMatchComponentOfArray() throws Exception {
        Type arrayType = new TypeLiteral<Foo<String>[]>(){}.getType();
        Type componentType = new TypeLiteral<Foo<String>>(){}.getType();
        Assert.assertFalse("array type should not match its component type", Reflections.matches(arrayType, componentType));
    }

    @Test
    public void testParameterizedArrayMatches() throws Exception {
        Type type = new TypeLiteral<Foo<Integer>[]>(){}.getType();
        Assert.assertTrue("type should match itself", Reflections.matches(type, type));
    }

    @Test
    public void testArraysDontMatch() throws Exception {
        Type type1 = new TypeLiteral<Foo<Integer>[]>(){}.getType();
        Type type2 = new TypeLiteral<Foo<String>[]>(){}.getType();
        Assert.assertFalse("Foo<Integer>[] should not match Foo<String>[]", Reflections.matches(type1, type2));
    }

    @Test
    public void testWildcardFooMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<?>>(){}.getType();
        Assert.assertTrue("Foo<?> should be assignable from Foo<?>", Reflections.matches(type, type));
    }

    @Test
    public void testWildcardFooMatchesStringFoo() throws Exception {
        Type stringFooType = new TypeLiteral<Foo<String>>(){}.getType();
        Type wildcardFooType = new TypeLiteral<Foo<?>>(){}.getType();
        Assert.assertTrue("Foo<?> should be assignable from Foo<String>", Reflections.matches(wildcardFooType, stringFooType));
    }

    @Test
    public void testWildcardFooArrayMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<?>[]>(){}.getType();
        Assert.assertTrue("Foo<?>[] should be assignable from itself", Reflections.matches(type, type));
    }

    @Test
    public void testWildcardFooArrayMatchesStringFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>(){}.getType();
        Type wildcardFooArrayType = new TypeLiteral<Foo<?>[]>(){}.getType();
        Assert.assertTrue("Foo<?>[] should be assignable from Foo<String>[]", Reflections.matches(wildcardFooArrayType, stringFooArrayType));
    }

    @Test
    public void testStringFooArrayDoesNotMatchWildcardFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>(){}.getType();
        Type wildcardFooArrayType = new TypeLiteral<Foo<?>[]>(){}.getType();
        Assert.assertFalse("Foo<String>[] should not be assignable from Foo<?>[]", Reflections.matches(stringFooArrayType, wildcardFooArrayType));
    }


    @Test
    public void testWildcardFooMatchesBoundedWildcardFoo() throws Exception {
        Type boundedWildcardFooType = new TypeLiteral<Foo<? extends Number>>(){}.getType();
        Type wildcardFooType = new TypeLiteral<Foo<?>>(){}.getType();
        Assert.assertTrue("Foo<?> should be assignable from Foo<? extends Number>", Reflections.matches(wildcardFooType, boundedWildcardFooType));
    }

    @Test
    public void testVariableFooMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<E>>(){}.getType();
        Assert.assertTrue("Foo<E> should be assignable from Foo<E>", Reflections.matches(type, type));
    }

    @Test
    public void testVariableFooMatchesStringFoo() throws Exception {
        Type stringFooType = new TypeLiteral<Foo<String>>(){}.getType();
        Type variableFooType = new TypeLiteral<Foo<E>>(){}.getType();
        Assert.assertTrue("Foo<E> should be assignable from Foo<String>", Reflections.matches(variableFooType, stringFooType));
    }

    @Test
    public void testStringFooMatchesVariableFoo() throws Exception {
        Type stringFooType = new TypeLiteral<Foo<String>>(){}.getType();
        Type variableFooType = new TypeLiteral<Foo<E>>(){}.getType();
        Assert.assertTrue("Foo<String> should match Foo<E>", Reflections.matches(stringFooType, variableFooType));
    }

    @Test
    public void testVariableFooArrayMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<E>[]>(){}.getType();
        Assert.assertTrue("Foo<E>[] should be assignable from Foo<E>[]", Reflections.matches(type, type));
    }

    @Test
    public void testVariableFooArrayMatchesStringFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>(){}.getType();
        Type variableFooArrayType = new TypeLiteral<Foo<E>[]>(){}.getType();
        Assert.assertTrue("Foo<E>[] should be assignable from Foo<String>[]", Reflections.matches(variableFooArrayType, stringFooArrayType));
    }

    @Test
    public void testRawRequiredTypeMatchesParameterizedBeanWithObjectTypeParameter() throws Exception {
        Assert.assertTrue("Foo<Object> should be assignable to Foo",
            Reflections.matches(
                Foo.class,
                new TypeLiteral<Foo<Object>>() { }.getType()));
    }

    @Test
    public <E> void testRawRequiredTypeMatchesParameterizedBeanWithUnboundedVariableTypeParameter() throws Exception {
        Assert.assertTrue("Foo<E> should be assignable to Foo",
            Reflections.matches(
                Foo.class,
                new TypeLiteral<Foo<E>>() { }.getType()));
    }

    @Ignore("WELD-1054")
    @Test
    public <F extends Number> void testParameterizedBeanWithBoundedVariableTypeParameterIsNotAssignableToRawRequiredType() throws Exception {
        Assert.assertFalse("Foo<F extends Number> should not be assignable to Foo",
            Reflections.matches(
                Foo.class,
                new TypeLiteral<Foo<F>>() { }.getType()));
    }

}