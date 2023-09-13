package org.jboss.weld.tests.unit.reflection.inheritance;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class TypeVariableResolverTest {

    @Test
    public void testConcreteType() throws Exception {
        assertTypeEquals(new TypeLiteral<Faz>() {
        }, A.class, A.class.getDeclaredField("faz"));
    }

    @Test
    public void testParameterizedConcreteType() throws Exception {
        assertTypeEquals(new TypeLiteral<Foo<String>>() {
        }, A.class, A.class.getDeclaredField("stringFoo"));
    }

    @Test
    public void testNestedParameterizedType() throws Exception {
        assertTypeEquals(new TypeLiteral<Foo<Foo<String>>>() {
        }, A.class, A.class.getDeclaredField("stringFooFoo"));
    }

    @Test
    public void testNestedParameterizedTypeWithVariable() throws Exception {
        assertTypeEquals(new TypeLiteral<Foo<Foo<Integer>>>() {
        }, BOfIntegerString.class, A.class.getDeclaredField("variableFooFoo"));
    }

    @Test
    public void testParameterizedTypeWithVariable() throws Exception {
        assertTypeEquals(new TypeLiteral<Foo<String>>() {
        }, BOfIntegerString.class, B.class.getDeclaredField("foo3"));
    }

    @Test
    public void testSuperSuperClass() throws Exception {
        assertTypeEquals(new TypeLiteral<Foo<Integer>>() {
        }, BOfIntegerString.class, A.class.getDeclaredField("foo1"));
    }

    @Test
    public void testSuperSuperClassWhereVariableIsDefinedInSuperClass() throws Exception {
        assertTypeEquals(new TypeLiteral<Foo<Double>>() {
        }, BOfIntegerString.class, A.class.getDeclaredField("foo2"));
    }

    @Test
    public void testStringFooArray() throws Exception {
        assertTypeEquals(new TypeLiteral<Foo<String>[]>() {
        }, BOfIntegerString.class, A.class.getDeclaredField("stringFooArray"));
    }

    @Test
    public void testVariableArray() throws Exception {
        assertTypeEquals(Integer[].class, BOfIntegerString.class, A.class.getDeclaredField("variableArray"));
    }

    @Test
    public void testTwoDimensionalVariableArray() throws Exception {
        assertTypeEquals(Integer[][].class, BOfIntegerString.class, A.class.getDeclaredField("twoDimensionalVariableArray"));
    }

    @Test
    public void testArrayWithParameterizedTypeWithVariable() throws Exception {
        assertTypeEquals(new TypeLiteral<Foo<Integer>[]>() {
        }, BOfIntegerString.class, A.class.getDeclaredField("foo1Array"));
    }

    @Test
    public void testVariable() throws Exception {
        assertTypeEquals(new TypeLiteral<Integer>() {
        }, BOfIntegerString.class, A.class.getDeclaredField("e1"));
    }

    @Test
    public void testSuperSuperSuperClass() throws Exception {
        assertTypeEquals(new TypeLiteral<Foo<Short>>() {
        }, COfByteShort.class, A.class.getDeclaredField("foo1"));
    }

    private void assertTypeEquals(TypeLiteral<?> expectedTypeLiteral, Class beanClass, Field field) {
        assertTypeEquals(expectedTypeLiteral.getType(), beanClass, field);
    }

    private void assertTypeEquals(Type expectedType, Class beanClass, Field field) {
        Type type = new HierarchyDiscovery(beanClass).resolveType(field.getGenericType());
        Assert.assertEquals(type, expectedType);
    }

}
