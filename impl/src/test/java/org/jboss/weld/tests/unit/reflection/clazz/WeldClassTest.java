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

import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

//@Artifact
public class WeldClassTest
{
	
   private final ClassTransformer transformer = new ClassTransformer("", new TypeStore());
   
   /*
    * description = "WELD-216"
    */
   @Test
   @Ignore // Broken
   public void testMemberClassWithGenericTypes()
   {
      AnnotatedType<?> at = WeldClassImpl.of("", new Kangaroo().procreate().getClass(), transformer);
      WeldClassImpl.of("", at, transformer);
   }
   
   /*
    * description = "WELD-216"
    */
   @Test
   @Ignore 
   /*
    *  Not isolated, depends on someone else initializing Containers.
    *  
    *  getUnproxyableClassException() catch(NoSuchMethodException)
    *           InstantiatorFactory.useInstantiators() <-- Needs Containers
    */
   public void testLocalClassWithGenericTypes()
   {
      AnnotatedType<?> at = WeldClassImpl.of("", new Koala().procreate().getClass(), transformer);
      WeldClassImpl.of("", at, transformer);
   }
   
   /*
    * description = "WELD-216"
    */
   @Test
   @Ignore 
   /*
    *  Not isolated, depends on someone else initializing Containers.
    *  
    *  getUnproxyableClassException() catch(NoSuchMethodException)
    *           InstantiatorFactory.useInstantiators() <-- Needs Containers
    */
   public void testAnonymousClassWithGenericTypes()
   {
      AnnotatedType<?> at = WeldClassImpl.of("", new Possum().procreate().getClass(), transformer);
      WeldClassImpl.of("", at, transformer);
   }
   
   @Test
   public void testDeclaredAnnotations()
   {
      WeldClass<Order> annotatedElement = WeldClassImpl.of("", Order.class, transformer);
      Assert.assertEquals(1, annotatedElement.getAnnotations().size());
      Assert.assertNotNull(annotatedElement.getAnnotation(Random.class));
      Assert.assertEquals(Order.class, annotatedElement.getJavaClass());
   }
   
   @Test
   public void testMetaAnnotations()
   {
      WeldClass<Order> annotatedElement = WeldClassImpl.of("", Order.class, transformer);
      Set<Annotation> annotations = annotatedElement.getMetaAnnotations(Qualifier.class);
      Assert.assertEquals(1, annotations.size());
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      Assert.assertEquals(Random.class, production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      WeldClass<Order> annotatedElement = WeldClassImpl.of("", Order.class, transformer);
      Assert.assertNull(annotatedElement.getAnnotation(Stereotype.class));
      Assert.assertEquals(0, annotatedElement.getMetaAnnotations(Stereotype.class).size());
      WeldClass<Antelope> classWithNoAnnotations = WeldClassImpl.of("", Antelope.class, transformer);
      Assert.assertEquals(0, classWithNoAnnotations.getAnnotations().size());
   }
   
   @Test
   public void testStackOverflow() throws Throwable
   {
      Type type = AdvancedMap.class.getMethod("getReallyAdvancedMap").getGenericReturnType();
      HierarchyDiscovery discovery = new HierarchyDiscovery(type);
      
      discovery.getTypeClosure();
   }

}
