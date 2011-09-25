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
package org.jboss.weld.tests.unit.util;

import java.util.Iterator;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;

import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.test.util.annotated.TestAnnotatedTypeBuilder;
import org.jboss.weld.util.AnnotatedTypes;
import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * Test comparison and id creation for AnnotatedTypes
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 *
 */
public class AnnotatedTypesTest
{
   private static String contextId = RegistrySingletonProvider.STATIC_INSTANCE;
   /**
    * tests the AnnotatedTypes.compareAnnotatedTypes 
    */
   @Test
   public void testComparison() throws SecurityException, NoSuchFieldException, NoSuchMethodException
   {
      //check that two weld classes on the same underlying are equal
      TypeStore ts = new TypeStore();
      ClassTransformer ct =new ClassTransformer(contextId, ts);
      WeldClass<Chair> chair1 = WeldClassImpl.of(contextId, Chair.class,ct);
      WeldClass<Chair> chair2 = WeldClassImpl.of(contextId, Chair.class,ct);
      Assert.assertTrue(AnnotatedTypes.compareAnnotatedTypes(chair1, chair2));
      
      //check that a different implementation of annotated type is equal to the weld implementation
      TestAnnotatedTypeBuilder<Chair> builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      builder.addToClass(new DefaultLiteral());
      builder.addToField(Chair.class.getField("legs"), new ProducesLiteral());
      builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral());
      AnnotatedType<Chair> chair3 = builder.create();
      Assert.assertTrue(AnnotatedTypes.compareAnnotatedTypes(chair1, chair3));
      
      //check that the implementation returns false if a field annotation changes
      builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      builder.addToClass(new DefaultLiteral());
      builder.addToField(Chair.class.getField("legs"), new DefaultLiteral());
      builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral());     
      chair3 = builder.create();
      Assert.assertFalse(AnnotatedTypes.compareAnnotatedTypes(chair1, chair3));
      
      //check that the implementation returns false if a class level annotation changes
      builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      builder.addToClass(new ProducesLiteral());
      builder.addToField(Chair.class.getField("legs"), new DefaultLiteral());
      builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral()); 
      chair3 = builder.create();
      Assert.assertFalse(AnnotatedTypes.compareAnnotatedTypes(chair1, chair3));
      
   }
   
   @Test
   public void testFieldId() throws SecurityException, NoSuchFieldException
   {
      TestAnnotatedTypeBuilder<Chair> builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      builder.addToField(Chair.class.getField("legs"), new ProducesLiteral());
      AnnotatedType<Chair> chair3 = builder.create();
      AnnotatedField<? super Chair> field = chair3.getFields().iterator().next();
      String id = AnnotatedTypes.createFieldId(field);
      Assert.assertEquals("wrong id for field :" + id,
            "org.jboss.weld.tests.unit.util.Chair.legs[@javax.enterprise.inject.Produces()]", id);
      
      builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      chair3 = builder.create();
      field = chair3.getFields().iterator().next();
      id = AnnotatedTypes.createFieldId(field);
      Assert.assertEquals("wrong id for field :" + id,
            "org.jboss.weld.tests.unit.util.Chair.legs", id);
      
      builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      builder.addToField(Chair.class.getField("legs"), new ComfyChairLiteral());
      chair3 = builder.create();
      field = chair3.getFields().iterator().next();
      id = AnnotatedTypes.createFieldId(field);
      Assert.assertEquals("wrong id for field :" + id,
            "org.jboss.weld.tests.unit.util.Chair.legs[@org.jboss.weld.tests.unit.util.ComfyChair(softness=1)]", id);
   }
   
   @Test
   public void testMethodId() throws SecurityException, NoSuchMethodException
   {
      TestAnnotatedTypeBuilder<Chair> builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral());
      AnnotatedType<Chair> chair3 = builder.create();
      Iterator<AnnotatedMethod<? super Chair>> it = chair3.getMethods().iterator();
      AnnotatedMethod<? super Chair> method = it.next();
      while(!method.getJavaMember().getName().equals("sit"))method = it.next();
      String id = AnnotatedTypes.createCallableId(method);
      Assert.assertEquals("wrong id for method :" + id, 
            "org.jboss.weld.tests.unit.util.Chair.sit[@javax.enterprise.inject.Produces()]()", id);
      
      builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      chair3 = builder.create();
      it = chair3.getMethods().iterator();
      method = it.next();
      while(!method.getJavaMember().getName().equals("sit"))method = it.next();
      id = AnnotatedTypes.createCallableId(method);
      Assert.assertEquals("wrong id for method :" + id,
            "org.jboss.weld.tests.unit.util.Chair.sit()", id);
      
      builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      builder.addToMethod(Chair.class.getMethod("sit"), new ComfyChairLiteral());
      chair3 = builder.create();
      it = chair3.getMethods().iterator();
      method = it.next();
      while(!method.getJavaMember().getName().equals("sit"))method = it.next();
      id = AnnotatedTypes.createCallableId(method);
      Assert.assertEquals("wrong id for method :" + id,
            "org.jboss.weld.tests.unit.util.Chair.sit[@org.jboss.weld.tests.unit.util.ComfyChair(softness=1)]()", id);
   }
   
   @Test
   public void testTypeId() throws SecurityException, NoSuchMethodException
   {
      TestAnnotatedTypeBuilder<Chair> builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral());
      AnnotatedType<Chair> chair3 = builder.create();
      String id = AnnotatedTypes.createTypeId(chair3);
      Assert.assertEquals("wrong id for type :" + id, 
            "org.jboss.weld.tests.unit.util.Chair{org.jboss.weld.tests.unit.util.Chair.sit[@javax.enterprise.inject.Produces()]();}", id);
      
      builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      chair3 = builder.create();
      id = AnnotatedTypes.createTypeId(chair3);
      Assert.assertEquals("wrong id for type :" + id,
            "org.jboss.weld.tests.unit.util.Chair{}", id);
      
      builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
      builder.addToMethod(Chair.class.getMethod("sit"), new ComfyChairLiteral());
      chair3 = builder.create();
      id = AnnotatedTypes.createTypeId(chair3);
      Assert.assertEquals("wrong id for type :" + id,
            "org.jboss.weld.tests.unit.util.Chair{org.jboss.weld.tests.unit.util.Chair.sit[@org.jboss.weld.tests.unit.util.ComfyChair(softness=1)]();}", id);
   }
   
   private static class DefaultLiteral extends AnnotationLiteral<Default> implements Default {}
   private static class ProducesLiteral extends AnnotationLiteral<Produces> implements Produces {}
   private static class ComfyChairLiteral extends AnnotationLiteral<ComfyChair> implements ComfyChair 
   {
      public int softness()
      {
         return 1;
      }
      
   }
}
