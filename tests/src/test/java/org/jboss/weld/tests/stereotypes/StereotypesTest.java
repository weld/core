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
