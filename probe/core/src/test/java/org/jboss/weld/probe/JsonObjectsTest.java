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

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import org.junit.Test;

/**
 *
 * @author Martin Kouba
 * @author Matej Novotny
 */
public class JsonObjectsTest {

    @Test
    public void testAnnotatedMethodToString() {
        assertEquals("void doSomething(@Observes String)", JsonObjects.annotatedMethodToString(getFooAnnotatedMethod("doSomething"), Foo.class).trim());
        assertEquals("public <T> T hello(T, Integer)", JsonObjects.annotatedMethodToString(getFooAnnotatedMethod("hello"), Foo.class).trim());
        assertEquals("int getAge(String)", JsonObjects.annotatedMethodToString(getFooAnnotatedMethod("getAge"), Foo.class).trim());
        assertEquals("static String[] getArray()", JsonObjects.annotatedMethodToString(getFooAnnotatedMethod("getArray"), Foo.class).trim());
    }

    @Test
    public void testAnnotatationToStringConversion() throws NoSuchMethodException {
        String expectedOutcome = "@org.jboss.weld.probe.JsonObjectsTest$SomeAnnotation(someInt=1, someString=\"bar\", someStringArray={\"charlie\", \"delta\"})";
        assertEquals(expectedOutcome, JsonObjects.annotationToString(getBarTypeAnnotation()));
        assertEquals(expectedOutcome, JsonObjects.annotationToString(getBarMethodParamAnnotation()));
        assertEquals(expectedOutcome, JsonObjects.annotationToString(getBarMethodAnnotation()));
    }

    private static AnnotatedMethod<Foo> getFooAnnotatedMethod(String name) {
        final Method method = findDeclaredFooMethod(name);
        return new AnnotatedMethod<Foo>() {

            @Override
            public List<AnnotatedParameter<Foo>> getParameters() {
                Type[] paramTypes = method.getGenericParameterTypes();
                if (paramTypes.length == 0) {
                    return Collections.emptyList();
                }
                List<AnnotatedParameter<Foo>> params = new ArrayList<>();
                for (int i = 0; i < paramTypes.length; i++) {
                    final Type baseType = paramTypes[i];
                    final Annotation[] annotations = method.getParameterAnnotations()[i];
                    params.add(new AnnotatedParameter<Foo>() {

                        @Override
                        public Type getBaseType() {
                            return baseType;
                        }

                        @Override
                        public Set<Type> getTypeClosure() {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Set<Annotation> getAnnotations() {
                            return new HashSet<>(Arrays.asList(annotations));
                        }

                        @Override
                        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public int getPosition() {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public AnnotatedCallable<Foo> getDeclaringCallable() {
                            throw new UnsupportedOperationException();
                        }
                    });
                }
                return params;
            }

            @Override
            public boolean isStatic() {
                throw new UnsupportedOperationException();
            }

            @Override
            public AnnotatedType<Foo> getDeclaringType() {
                return new AnnotatedType<Foo>() {

                    @Override
                    public Type getBaseType() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Set<Type> getTypeClosure() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Set<Annotation> getAnnotations() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Class<Foo> getJavaClass() {
                        return Foo.class;
                    }

                    @Override
                    public Set<AnnotatedConstructor<Foo>> getConstructors() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Set<AnnotatedMethod<? super Foo>> getMethods() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Set<AnnotatedField<? super Foo>> getFields() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public Type getBaseType() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<Type> getTypeClosure() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<Annotation> getAnnotations() {
                return new HashSet<>(Arrays.asList(method.getAnnotations()));
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Method getJavaMember() {
                return method;
            }
        };
    }

    private static Method findDeclaredFooMethod(String name) {
        for (Method fooMethod : Foo.class.getDeclaredMethods()) {
            if (fooMethod.getName().equals(name)) {
                return fooMethod;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static class Foo {

        void doSomething(@Observes String name) {
        }

        public <T> T hello(T type, Integer age) {
            return null;
        }

        int getAge(String name) {
            return 1;
        }

        static String[] getArray() {
            return null;
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    private static @interface SomeAnnotation {

        String someString();

        int someInt();

        String[] someStringArray();
    }

    @SuppressWarnings("unused")
    @SomeAnnotation(
        someString = "bar", someInt = 1, someStringArray = { "charlie", "delta" })
    private static class Bar {

        @SomeAnnotation(
            someString = "bar", someInt = 1, someStringArray = { "charlie", "delta" })
        public void hello() {

        }

        public void hello2(@SomeAnnotation(someString = "bar", someInt = 1, someStringArray = { "charlie", "delta" }) String s) {

        }
    }

    private static Annotation getBarTypeAnnotation() {
        return Bar.class.getAnnotation(SomeAnnotation.class);
    }

    private static Annotation getBarMethodAnnotation() throws NoSuchMethodException {
        return Bar.class.getDeclaredMethod("hello").getAnnotation(SomeAnnotation.class);
    }

    private static Annotation getBarMethodParamAnnotation() throws NoSuchMethodException {
        return Bar.class.getDeclaredMethod("hello2", String.class).getParameters()[0].getAnnotation(SomeAnnotation.class);
    }
}
