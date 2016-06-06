/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.probe;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.util.ForwardingBeanManager;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class ParsersTest {

    @Test
    public void testParseRequiredType() {
        assertEquals(Long.class, Parsers.parseType("java.lang.Long", null));
        assertNull(Parsers.parseType("java.lang.Comparable<Long>", null));
        Type type = Parsers.parseType("java.lang.Comparable<java.lang.Long>", null);
        assertTrue(type instanceof ParameterizedType);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        assertEquals(Comparable.class, parameterizedType.getRawType());
        assertEquals(1, parameterizedType.getActualTypeArguments().length);
        assertEquals(Long.class, parameterizedType.getActualTypeArguments()[0]);
        type = Parsers.parseType("java.util.Map<java.lang.String,java.lang.Boolean>", null);
        assertTrue(type instanceof ParameterizedType);
        parameterizedType = (ParameterizedType) type;
        assertEquals(Map.class, parameterizedType.getRawType());
        assertEquals(2, parameterizedType.getActualTypeArguments().length);
        assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);
        assertEquals(Boolean.class, parameterizedType.getActualTypeArguments()[1]);
        // Nested parameterized type
        type = Parsers.parseType("java.util.Map<java.lang.String,java.util.List<java.lang.Integer>>", null);
        assertTrue(type instanceof ParameterizedType);
        parameterizedType = (ParameterizedType) type;
        assertEquals(Map.class, parameterizedType.getRawType());
        assertEquals(2, parameterizedType.getActualTypeArguments().length);
        assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);
        Type nestedType = parameterizedType.getActualTypeArguments()[1];
        assertTrue(nestedType instanceof ParameterizedType);
        ParameterizedType nestedParameterizedType = (ParameterizedType) nestedType;
        assertEquals(List.class, nestedParameterizedType.getRawType());
        assertEquals(1, nestedParameterizedType.getActualTypeArguments().length);
        assertEquals(Integer.class, nestedParameterizedType.getActualTypeArguments()[0]);
        // Wildcards
        type = Parsers.parseType("java.lang.Comparable<? extends java.lang.Number>", null);
        assertTrue(type instanceof ParameterizedType);
        parameterizedType = (ParameterizedType) type;
        assertEquals(1, parameterizedType.getActualTypeArguments().length);
        Type typeArgument = parameterizedType.getActualTypeArguments()[0];
        assertTrue(typeArgument instanceof WildcardType);
        WildcardType wildcardType = (WildcardType) typeArgument;
        assertEquals(1, wildcardType.getUpperBounds().length);
        assertEquals(0, wildcardType.getLowerBounds().length);
        assertEquals(Number.class, wildcardType.getUpperBounds()[0]);
        type = Parsers.parseType("java.lang.Comparable<? super java.lang.Integer>", null);
        assertTrue(type instanceof ParameterizedType);
        parameterizedType = (ParameterizedType) type;
        assertEquals(1, parameterizedType.getActualTypeArguments().length);
        typeArgument = parameterizedType.getActualTypeArguments()[0];
        assertTrue(typeArgument instanceof WildcardType);
        wildcardType = (WildcardType) typeArgument;
        assertEquals(1, wildcardType.getUpperBounds().length);
        assertEquals(1, wildcardType.getLowerBounds().length);
        assertEquals(Object.class, wildcardType.getUpperBounds()[0]);
        assertEquals(Integer.class, wildcardType.getLowerBounds()[0]);
        // Arrays
        type = Parsers.parseType("java.util.List<java.lang.Integer>[]", null);
        assertTrue(type instanceof GenericArrayType);
        GenericArrayType arrayType = (GenericArrayType) type;
        assertTrue(arrayType.getGenericComponentType() instanceof ParameterizedType);
        parameterizedType = (ParameterizedType) arrayType.getGenericComponentType();
        assertEquals(List.class, nestedParameterizedType.getRawType());
        assertEquals(1, nestedParameterizedType.getActualTypeArguments().length);
        assertEquals(Integer.class, nestedParameterizedType.getActualTypeArguments()[0]);
    }

    @Test
    public void testParseQualifiers() {
        List<QualifierInstance> instances = Parsers.parseQualifiers("", null, testManager());
        assertEquals(0, instances.size());
        instances = Parsers.parseQualifiers("javax.enterprise.inject.Any", null, testManager());
        assertEquals(1, instances.size());
        assertEquals(Any.class, instances.get(0).getAnnotationClass());
        instances = Parsers.parseQualifiers("javax.enterprise.inject.Any,javax.inject.Named(value=\"foo\")", null, testManager());
        assertEquals(2, instances.size());
        assertEquals(Any.class, instances.get(0).getAnnotationClass());
        // Named does not need MetaAnnotationStore
        assertEquals(QualifierInstance.of(new NamedLiteral("foo"), null), instances.get(1));
        instances = Parsers.parseQualifiers("org.jboss.weld.probe.ParsersTest$Foo(age=5,condition=false,type=java.lang.String,stringArray=[])", null, testManager());
        assertEquals(1, instances.size());
        assertNotNull(instances.get(0));
        assertEquals(5, instances.get(0).getValue("age"));
        assertEquals(false, instances.get(0).getValue("condition"));
        assertEquals(String.class, instances.get(0).getValue("type"));
        assertEquals(15, instances.get(0).getValue("defaultAge"));
    }
    @SuppressWarnings("serial")
    BeanManager testManager() {
        return new ForwardingBeanManager() {
            @Override
            public BeanManager delegate() {
                return null;
            }

            @Override
            public boolean isQualifier(Class<? extends Annotation> annotationType) {
                return true;
            }
        };
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({ TYPE, METHOD, FIELD, PARAMETER })
    @Documented
    public @interface Foo {

        int age();

        boolean condition();

        Class<?> type();

        int defaultAge() default 15;

        @Nonbinding
        String[] stringArray();

    }

}
