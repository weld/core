/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.test.unit.annotated;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author kkahn
 * 
 */
@Artifact
public class DeclaringTypeTest extends AbstractWebBeansTest
{

   @Test
   public void testInheritance()
   {
      AnnotatedType<Child> type = getCurrentManager().createAnnotatedType(Child.class);
      assert type.getConstructors().size() == 1;
      assert type.getFields().size() == 1;
      for (AnnotatedField<? super Child> field : type.getFields())
      {
         if (field.getJavaMember().getName().equals("parent"))
         {
            Assert.assertEquals(Parent.class, field.getJavaMember().getDeclaringClass()); // OK
                                                                                          // -
                                                                                          // Returns
                                                                                          // Parent
            Assert.assertEquals(Parent.class, field.getDeclaringType().getJavaClass()); // FAIL
                                                                                        // -
                                                                                        // Returns
                                                                                        // Child
         }
         else
         {
            Assert.fail("Unknown field " + field.getJavaMember());
         }
      }

      assert type.getMethods().size() == 1;
      for (AnnotatedMethod<? super Child> method : type.getMethods())
      {
         if (method.getJavaMember().getName().equals("parentMethod"))
         {
            Assert.assertEquals(Parent.class, method.getJavaMember().getDeclaringClass()); // OK
                                                                                           // -
                                                                                           // /Returns
                                                                                           // Parent
            Assert.assertEquals(Parent.class, method.getDeclaringType().getJavaClass()); // FAIL
                                                                                         // -
                                                                                         // Returns
                                                                                         // Child
         }
         else
         {
            Assert.fail("Unknown method " + method.getJavaMember());
         }
      }
   }

}
