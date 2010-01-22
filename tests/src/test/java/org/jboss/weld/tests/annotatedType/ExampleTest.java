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
package org.jboss.weld.tests.annotatedType;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@Artifact
public class ExampleTest extends AbstractWeldTest
{
   @Test
   public void testAnnotatedCallableGetParameters() throws Exception 
   {
      AnnotatedType<Bean> type = getCurrentManager().createAnnotatedType(Bean.class);
      
      assertNoAnnotations(type);
      
      Assert.assertEquals(1, type.getConstructors().size());
      for (AnnotatedConstructor<Bean> ctor : type.getConstructors())
      {
         assertNoAnnotations(ctor);
         
         for (AnnotatedParameter<Bean> param : ctor.getParameters())
         {
            assertNoAnnotations(param);
         }
      }
      
      Assert.assertEquals(1, type.getMethods().size());
      for (AnnotatedMethod<? super Bean> method : type.getMethods())
      {
         assertNoAnnotations(method);
         
         for (AnnotatedParameter<? super Bean> param : method.getParameters())
         {
            assertNoAnnotations(param);
         }
      }
      
      Assert.assertEquals(1, type.getFields().size());
      for (AnnotatedField<? super Bean> field : type.getFields())
      {
         assertNoAnnotations(field);
      }
   }

   private void assertNoAnnotations(Annotated annotated)
   {
      Assert.assertEquals(0, annotated.getAnnotations().size());
   }
}
