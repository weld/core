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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.enterprise.inject.spi.AnnotatedType;

/**
 * Class for constructing a new AnnotatedType. A new instance of builder must be
 * used for each annotated type.
 * <p/>
 * No annotations will be read from the underlying class definition, all
 * annotations must be added explicitly
 *
 * @author Stuart Douglas
 * @author Pete Muir
 */
public class TestAnnotatedTypeBuilder<X> {
    private Map<Field, TestAnnotationBuilder> fields = new HashMap<Field, TestAnnotationBuilder>();
    private Map<Method, TestAnnotationBuilder> methods = new HashMap<Method, TestAnnotationBuilder>();
    private Map<Method, Map<Integer, TestAnnotationBuilder>> methodParameters = new HashMap<Method, Map<Integer, TestAnnotationBuilder>>();
    private Map<Constructor<X>, TestAnnotationBuilder> constructors = new HashMap<Constructor<X>, TestAnnotationBuilder>();
    private Map<Constructor<X>, Map<Integer, TestAnnotationBuilder>> constructorParameters = new HashMap<Constructor<X>, Map<Integer, TestAnnotationBuilder>>();
    private TestAnnotationBuilder typeAnnotations = new TestAnnotationBuilder();
    private Class<X> underlying;

    public TestAnnotatedTypeBuilder(Class<X> underlying) {
        this.underlying = underlying;

    }

    public TestAnnotatedTypeBuilder<X> addToClass(Annotation a) {
        typeAnnotations.add(a);
        return this;
    }

    public TestAnnotatedTypeBuilder<X> addToField(Field field, Annotation a) {
        TestAnnotationBuilder annotations = fields.get(field);
        if (annotations == null) {
            annotations = new TestAnnotationBuilder();
            fields.put(field, annotations);
        }
        annotations.add(a);
        return this;
    }

    public TestAnnotatedTypeBuilder<X> addToMethod(Method method, Annotation a) {
        TestAnnotationBuilder annotations = methods.get(method);
        if (annotations == null) {
            annotations = new TestAnnotationBuilder();
            methods.put(method, annotations);
        }
        annotations.add(a);
        return this;
    }

    public TestAnnotatedTypeBuilder<X> addToMethodParameter(Method method, int parameter, Annotation a) {
        Map<Integer, TestAnnotationBuilder> anmap = methodParameters.get(method);
        if (anmap == null) {
            anmap = new HashMap<Integer, TestAnnotationBuilder>();
            methodParameters.put(method, anmap);
        }
        TestAnnotationBuilder annotations = anmap.get(parameter);
        if (annotations == null) {
            annotations = new TestAnnotationBuilder();
            anmap.put(parameter, annotations);
        }
        annotations.add(a);
        return this;
    }

    public TestAnnotatedTypeBuilder<X> addToConstructor(Constructor<X> constructor, Annotation a) {
        TestAnnotationBuilder annotations = constructors.get(constructor);
        if (annotations == null) {
            annotations = new TestAnnotationBuilder();
            constructors.put(constructor, annotations);
        }
        annotations.add(a);
        return this;
    }

    public TestAnnotatedTypeBuilder<X> addToConstructorParameter(Constructor<X> constructor, int parameter, Annotation a) {
        Map<Integer, TestAnnotationBuilder> anmap = constructorParameters.get(constructor);
        if (anmap == null) {
            anmap = new HashMap<Integer, TestAnnotationBuilder>();
            constructorParameters.put(constructor, anmap);
        }
        TestAnnotationBuilder annotations = anmap.get(parameter);
        if (annotations == null) {
            annotations = new TestAnnotationBuilder();
            anmap.put(parameter, annotations);
        }
        annotations.add(a);
        return this;
    }

    public AnnotatedType<X> create() {
        Map<Constructor<X>, Map<Integer, TestAnnotationStore>> constructorParameterAnnotations = new HashMap<Constructor<X>, Map<Integer, TestAnnotationStore>>();
        Map<Constructor<X>, TestAnnotationStore> constructorAnnotations = new HashMap<Constructor<X>, TestAnnotationStore>();
        Map<Method, Map<Integer, TestAnnotationStore>> methodParameterAnnotations = new HashMap<Method, Map<Integer, TestAnnotationStore>>();
        Map<Method, TestAnnotationStore> methodAnnotations = new HashMap<Method, TestAnnotationStore>();
        Map<Field, TestAnnotationStore> fieldAnnotations = new HashMap<Field, TestAnnotationStore>();

        for (Entry<Field, TestAnnotationBuilder> e : fields.entrySet()) {
            fieldAnnotations.put(e.getKey(), e.getValue().create());
        }

        for (Entry<Method, TestAnnotationBuilder> e : methods.entrySet()) {
            methodAnnotations.put(e.getKey(), e.getValue().create());
        }
        for (Entry<Method, Map<Integer, TestAnnotationBuilder>> e : methodParameters.entrySet()) {
            Map<Integer, TestAnnotationStore> parameterAnnotations = new HashMap<Integer, TestAnnotationStore>();
            methodParameterAnnotations.put(e.getKey(), parameterAnnotations);
            for (Entry<Integer, TestAnnotationBuilder> pe : e.getValue().entrySet()) {
                parameterAnnotations.put(pe.getKey(), pe.getValue().create());
            }
        }

        for (Entry<Constructor<X>, TestAnnotationBuilder> e : constructors.entrySet()) {
            constructorAnnotations.put(e.getKey(), e.getValue().create());
        }
        for (Entry<Constructor<X>, Map<Integer, TestAnnotationBuilder>> e : constructorParameters.entrySet()) {
            Map<Integer, TestAnnotationStore> parameterAnnotations = new HashMap<Integer, TestAnnotationStore>();
            constructorParameterAnnotations.put(e.getKey(), parameterAnnotations);
            for (Entry<Integer, TestAnnotationBuilder> pe : e.getValue().entrySet()) {
                parameterAnnotations.put(pe.getKey(), pe.getValue().create());
            }
        }

        return new TestAnnotatedType<X>(underlying, typeAnnotations.create(), fieldAnnotations, methodAnnotations,
                methodParameterAnnotations, constructorAnnotations, constructorParameterAnnotations);
    }

}
