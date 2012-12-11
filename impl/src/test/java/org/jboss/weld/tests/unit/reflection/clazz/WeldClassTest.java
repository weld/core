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

import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Qualifier;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotatedTypeImpl;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.ReflectionCacheFactory;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.junit.Assert;
import org.junit.Test;

public class WeldClassTest {

    private final TypeStore typeStore = new TypeStore();
    private final ClassTransformer transformer = new ClassTransformer(typeStore, new SharedObjectCache(), ReflectionCacheFactory.newInstance(typeStore));

    /*
    * description = "WELD-216"
    */
    @Test
    public void testMemberClassWithGenericTypes() {
        AnnotatedType<?> at = EnhancedAnnotatedTypeImpl.of(BackedAnnotatedType.of(new Kangaroo().procreate().getClass(), transformer.getSharedObjectCache(), transformer.getReflectionCache()), transformer);
        EnhancedAnnotatedTypeImpl.of(UnbackedAnnotatedType.of(at), transformer);
    }

    /*
    * description = "WELD-216"
    */
    @Test
    /*
    *  Not isolated, depends on someone else initializing Containers.
    *
    *  getUnproxyableClassException() catch(NoSuchMethodException)
    *           InstantiatorFactory.useInstantiators() <-- Needs Containers
    */
    public void testLocalClassWithGenericTypes() {
        AnnotatedType<?> at = EnhancedAnnotatedTypeImpl.of(BackedAnnotatedType.of(new Koala().procreate().getClass(), transformer.getSharedObjectCache(), transformer.getReflectionCache()), transformer);
        EnhancedAnnotatedTypeImpl.of(UnbackedAnnotatedType.of(at), transformer);
    }

    /*
    * description = "WELD-216"
    */
    @Test
    /*
    *  Not isolated, depends on someone else initializing Containers.
    *
    *  getUnproxyableClassException() catch(NoSuchMethodException)
    *           InstantiatorFactory.useInstantiators() <-- Needs Containers
    */
    public void testAnonymousClassWithGenericTypes() {
        AnnotatedType<?> at = EnhancedAnnotatedTypeImpl.of(BackedAnnotatedType.of(new Possum().procreate().getClass(), transformer.getSharedObjectCache(), transformer.getReflectionCache()), transformer);
        EnhancedAnnotatedTypeImpl.of(UnbackedAnnotatedType.of(at), transformer);
    }

    @Test
    public void testDeclaredAnnotations() {
        EnhancedAnnotatedType<Order> annotatedElement = transformer.getEnhancedAnnotatedType(Order.class);
        Assert.assertEquals(1, annotatedElement.getAnnotations().size());
        Assert.assertNotNull(annotatedElement.getAnnotation(Random.class));
        Assert.assertEquals(Order.class, annotatedElement.getJavaClass());
    }

    @Test
    public void testMetaAnnotations() {
        EnhancedAnnotatedType<Order> annotatedElement = transformer.getEnhancedAnnotatedType(Order.class);
        Set<Annotation> annotations = annotatedElement.getMetaAnnotations(Qualifier.class);
        Assert.assertEquals(1, annotations.size());
        Iterator<Annotation> it = annotations.iterator();
        Annotation production = it.next();
        Assert.assertEquals(Random.class, production.annotationType());
    }

    @Test
    public void testEmpty() {
        EnhancedAnnotatedType<Order> annotatedElement = transformer.getEnhancedAnnotatedType(Order.class);
        Assert.assertNull(annotatedElement.getAnnotation(Stereotype.class));
        Assert.assertEquals(0, annotatedElement.getMetaAnnotations(Stereotype.class).size());
        EnhancedAnnotatedType<Antelope> classWithNoAnnotations = transformer.getEnhancedAnnotatedType(Antelope.class);
        Assert.assertEquals(0, classWithNoAnnotations.getAnnotations().size());
    }

    @Test
    public void testStackOverflow() throws Throwable {
        Type type = AdvancedMap.class.getMethod("getReallyAdvancedMap").getGenericReturnType();
        HierarchyDiscovery discovery = new HierarchyDiscovery(type);

        discovery.getTypeClosure();
    }

}
