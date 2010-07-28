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
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Qualifier;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.testng.annotations.Test;

@Artifact
public class WeldClassTest
{
	
   private final ClassTransformer transformer = new ClassTransformer(new TypeStore());
   
   @Test(groups = "broken", description="WELD-216")
   public void testMemberClassWithGenericTypes()
   {
      AnnotatedType at = WeldClassImpl.of(new Kangaroo().procreate().getClass(), transformer);
      WeldClassImpl.of(at, transformer);
   }
   
   @Test(description="WELD-216")
   public void testLocalClassWithGenericTypes()
   {
      AnnotatedType at = WeldClassImpl.of(new Koala().procreate().getClass(), transformer);
      WeldClassImpl.of(at, transformer);
   }
   
   @Test(description="WELD-216")
   public void testAnonymousClassWithGenericTypes()
   {
      AnnotatedType at = WeldClassImpl.of(new Possum().procreate().getClass(), transformer);
      WeldClassImpl.of(at, transformer);
   }
   
   @Test
   public void testDeclaredAnnotations()
   {
      WeldClass<Order> annotatedElement = WeldClassImpl.of(Order.class, transformer);
      assert annotatedElement.getAnnotations().size() == 1;
      assert annotatedElement.getAnnotation(Random.class) != null;
      assert annotatedElement.getJavaClass().equals(Order.class);
   }
   
   @Test
   public void testMetaAnnotations()
   {
      WeldClass<Order> annotatedElement = WeldClassImpl.of(Order.class, transformer);
      Set<Annotation> annotations = annotatedElement.getMetaAnnotations(Qualifier.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Random.class.equals(production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      WeldClass<Order> annotatedElement = WeldClassImpl.of(Order.class, transformer);
      assert annotatedElement.getAnnotation(Stereotype.class) == null;
      assert annotatedElement.getMetaAnnotations(Stereotype.class).size() == 0;
      WeldClass<Antelope> classWithNoAnnotations = WeldClassImpl.of(Antelope.class, transformer);
      assert classWithNoAnnotations.getAnnotations().size() == 0;
   }

}
