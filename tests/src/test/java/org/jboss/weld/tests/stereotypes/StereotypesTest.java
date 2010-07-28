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

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.StereotypeModel;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class StereotypesTest extends AbstractWeldTest
{
	
   private final ClassTransformer transformer = new ClassTransformer(new TypeStore());
	
   @Test
   public void testAnimalStereotype()
   {
      StereotypeModel<AnimalStereotype> animalStereotype = new StereotypeModel<AnimalStereotype>(AnimalStereotype.class, transformer);
      assert animalStereotype.getDefaultScopeType().annotationType().equals(RequestScoped.class);
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert !animalStereotype.isBeanNameDefaulted();
      assert !animalStereotype.isAlternative();
   }
   
   @Test
   public void testAnimalOrderStereotype()
   {
      StereotypeModel<AnimalOrderStereotype> animalStereotype = new StereotypeModel<AnimalOrderStereotype>(AnimalOrderStereotype.class, transformer);
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert !animalStereotype.isBeanNameDefaulted();
      assert !animalStereotype.isAlternative();
   }
   
   @Test
   public void testRequestScopedAnimalStereotype()
   {
      StereotypeModel<RequestScopedAnimalStereotype> animalStereotype = new StereotypeModel<RequestScopedAnimalStereotype>(RequestScopedAnimalStereotype.class, transformer);
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert !animalStereotype.isBeanNameDefaulted();
      assert !animalStereotype.isAlternative();
   }
      
}
