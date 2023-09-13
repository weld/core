/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.test.util.annotated;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;

/**
 * AnnotatedType implementation for adding beans in the BeforeBeanDiscovery
 * event
 *
 * @author Stuart Douglas
 */
class TestAnnotatedType<X> extends AbstractTestAnnotatedElement implements AnnotatedType<X> {

    private final Set<AnnotatedConstructor<X>> constructors;
    private final Set<AnnotatedField<? super X>> fields;
    private final Set<AnnotatedMethod<? super X>> methods;

    private final Class<X> javaClass;

    TestAnnotatedType(Class<X> clazz, TestAnnotationStore typeAnnotations, Map<Field, TestAnnotationStore> fieldAnnotations,
            Map<Method, TestAnnotationStore> methodAnnotations,
            Map<Method, Map<Integer, TestAnnotationStore>> methodParameterAnnotations,
            Map<Constructor<X>, TestAnnotationStore> constructorAnnotations,
            Map<Constructor<X>, Map<Integer, TestAnnotationStore>> constructorParameterAnnotations) {
        super(clazz, typeAnnotations);
        this.javaClass = clazz;
        this.constructors = new HashSet<AnnotatedConstructor<X>>();
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            TestAnnotatedConstructor<X> nc = new TestAnnotatedConstructor<X>(this, c, constructorAnnotations.get(c),
                    constructorParameterAnnotations.get(c));
            constructors.add(nc);
        }
        this.methods = new HashSet<AnnotatedMethod<? super X>>();
        for (Method m : clazz.getDeclaredMethods()) {
            TestAnnotatedMethod<X> met = new TestAnnotatedMethod<X>(this, m, methodAnnotations.get(m),
                    methodParameterAnnotations.get(m));
            methods.add(met);
        }
        this.fields = new HashSet<AnnotatedField<? super X>>();
        for (Field f : clazz.getDeclaredFields()) {
            TestAnnotatedField<X> b = new TestAnnotatedField<X>(this, f, fieldAnnotations.get(f));
            fields.add(b);
        }
    }

    public Set<AnnotatedConstructor<X>> getConstructors() {
        return Collections.unmodifiableSet(constructors);
    }

    public Set<AnnotatedField<? super X>> getFields() {
        return Collections.unmodifiableSet(fields);
    }

    public Class<X> getJavaClass() {
        return javaClass;
    }

    public Set<AnnotatedMethod<? super X>> getMethods() {
        return Collections.unmodifiableSet(methods);
    }

}
