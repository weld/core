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
package org.jboss.weld.tests.stereotypes;

import javax.enterprise.context.RequestScoped;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.StereotypeModel;
import org.jboss.weld.resources.ClassTransformer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class StereotypesTest
{
   @Deployment
   public static Archive<?> deploy()
   {
      return ShrinkWrap.create(BeanArchive.class)
         .addPackage(StereotypesTest.class.getPackage());
   }

   private final ClassTransformer transformer = new ClassTransformer("STATIC_INSTANCE", new TypeStore());

   @Test
   public void testAnimalStereotype()
   {
      StereotypeModel<AnimalStereotype> animalStereotype = new StereotypeModel<AnimalStereotype>(AnimalStereotype.class, transformer);
      Assert.assertEquals(RequestScoped.class, animalStereotype.getDefaultScopeType().annotationType());
      Assert.assertEquals(0, animalStereotype.getInterceptorBindings().size());
      Assert.assertFalse(animalStereotype.isBeanNameDefaulted());
      Assert.assertFalse(animalStereotype.isAlternative());
   }

   @Test
   public void testAnimalOrderStereotype()
   {
      StereotypeModel<AnimalOrderStereotype> animalStereotype = new StereotypeModel<AnimalOrderStereotype>(AnimalOrderStereotype.class, transformer);
      Assert.assertNull(animalStereotype.getDefaultScopeType());
      Assert.assertEquals(0, animalStereotype.getInterceptorBindings().size());
      Assert.assertFalse(animalStereotype.isBeanNameDefaulted());
      Assert.assertFalse(animalStereotype.isAlternative());
   }

   @Test
   public void testRequestScopedAnimalStereotype()
   {
      StereotypeModel<RequestScopedAnimalStereotype> animalStereotype = new StereotypeModel<RequestScopedAnimalStereotype>(RequestScopedAnimalStereotype.class, transformer);
      Assert.assertNull(animalStereotype.getDefaultScopeType());
      Assert.assertEquals(0, animalStereotype.getInterceptorBindings().size());
      Assert.assertFalse(animalStereotype.isBeanNameDefaulted());
      Assert.assertFalse(animalStereotype.isAlternative());
   }
}
