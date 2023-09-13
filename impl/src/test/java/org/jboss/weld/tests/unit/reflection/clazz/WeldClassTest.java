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
package org.jboss.weld.tests.unit.reflection.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;

import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.inject.Qualifier;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.ReflectionCacheFactory;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.annotated.ForwardingAnnotatedType;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.junit.Assert;
import org.junit.Test;

public class WeldClassTest {

    private final TypeStore typeStore = new TypeStore();
    private final ClassTransformer transformer = new ClassTransformer(typeStore, new SharedObjectCache(),
            ReflectionCacheFactory.newInstance(typeStore), RegistrySingletonProvider.STATIC_INSTANCE);

    /*
     * description = "WELD-216"
     */
    @Test
    public void testMemberClassWithGenericTypes() {
        final AnnotatedType<?> at = transformer.getEnhancedAnnotatedType(new Kangaroo().procreate().getClass(),
                AnnotatedTypeIdentifier.NULL_BDA_ID);
        transformer.getEnhancedAnnotatedType(new ForwardingAnnotatedType() {

            @Override
            public AnnotatedType delegate() {
                return at;
            }
        }, AnnotatedTypeIdentifier.NULL_BDA_ID);
    }

    /*
     * description = "WELD-216"
     */
    @Test
    /*
     * Not isolated, depends on someone else initializing Containers.
     *
     * getUnproxyableClassException() catch(NoSuchMethodException)
     * InstantiatorFactory.useInstantiators() <-- Needs Containers
     */
    public void testLocalClassWithGenericTypes() {
        final AnnotatedType<?> at = transformer.getEnhancedAnnotatedType(new Koala().procreate().getClass(),
                AnnotatedTypeIdentifier.NULL_BDA_ID);
        transformer.getEnhancedAnnotatedType(new ForwardingAnnotatedType() {

            @Override
            public AnnotatedType delegate() {
                return at;
            }
        }, AnnotatedTypeIdentifier.NULL_BDA_ID);
    }

    /*
     * description = "WELD-216"
     */
    @Test
    /*
     * Not isolated, depends on someone else initializing Containers.
     *
     * getUnproxyableClassException() catch(NoSuchMethodException)
     * InstantiatorFactory.useInstantiators() <-- Needs Containers
     */
    public void testAnonymousClassWithGenericTypes() {
        final AnnotatedType<?> at = transformer.getEnhancedAnnotatedType(new Possum().procreate().getClass(),
                AnnotatedTypeIdentifier.NULL_BDA_ID);
        transformer.getEnhancedAnnotatedType(new ForwardingAnnotatedType() {

            @Override
            public AnnotatedType delegate() {
                return at;
            }
        }, AnnotatedTypeIdentifier.NULL_BDA_ID);
    }

    @Test
    public void testDeclaredAnnotations() {
        EnhancedAnnotatedType<Order> annotatedElement = transformer.getEnhancedAnnotatedType(Order.class,
                AnnotatedTypeIdentifier.NULL_BDA_ID);
        Assert.assertEquals(1, annotatedElement.getAnnotations().size());
        Assert.assertNotNull(annotatedElement.getAnnotation(Random.class));
        Assert.assertEquals(Order.class, annotatedElement.getJavaClass());
    }

    @Test
    public void testMetaAnnotations() {
        EnhancedAnnotatedType<Order> annotatedElement = transformer.getEnhancedAnnotatedType(Order.class,
                AnnotatedTypeIdentifier.NULL_BDA_ID);
        Set<Annotation> annotations = annotatedElement.getMetaAnnotations(Qualifier.class);
        Assert.assertEquals(1, annotations.size());
        Iterator<Annotation> it = annotations.iterator();
        Annotation production = it.next();
        Assert.assertEquals(Random.class, production.annotationType());
    }

    @Test
    public void testEmpty() {
        EnhancedAnnotatedType<Order> annotatedElement = transformer.getEnhancedAnnotatedType(Order.class,
                AnnotatedTypeIdentifier.NULL_BDA_ID);
        Assert.assertNull(annotatedElement.getAnnotation(Stereotype.class));
        Assert.assertEquals(0, annotatedElement.getMetaAnnotations(Stereotype.class).size());
        EnhancedAnnotatedType<Antelope> classWithNoAnnotations = transformer.getEnhancedAnnotatedType(Antelope.class,
                AnnotatedTypeIdentifier.NULL_BDA_ID);
        Assert.assertEquals(0, classWithNoAnnotations.getAnnotations().size());
    }

    @Test
    public void testStackOverflow() throws Throwable {
        Type type = AdvancedMap.class.getMethod("getReallyAdvancedMap").getGenericReturnType();
        HierarchyDiscovery discovery = new HierarchyDiscovery(type);

        discovery.getTypeClosure();
    }

}
