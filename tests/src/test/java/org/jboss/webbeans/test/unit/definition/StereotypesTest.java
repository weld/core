package org.jboss.webbeans.test.unit.definition;

import javax.enterprise.context.RequestScoped;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.metadata.TypeStore;
import org.jboss.webbeans.metadata.cache.StereotypeModel;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class StereotypesTest extends AbstractWebBeansTest
{
	
   private final ClassTransformer transformer = new ClassTransformer(new TypeStore());
	
   @Test
   public void testAnimalStereotype()
   {
      StereotypeModel<AnimalStereotype> animalStereotype = new StereotypeModel<AnimalStereotype>(AnimalStereotype.class, transformer);
      assert animalStereotype.getDefaultScopeType().annotationType().equals(RequestScoped.class);
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert !animalStereotype.isBeanNameDefaulted();
      assert !animalStereotype.isPolicy();
   }
   
   @Test
   public void testAnimalOrderStereotype()
   {
      StereotypeModel<AnimalOrderStereotype> animalStereotype = new StereotypeModel<AnimalOrderStereotype>(AnimalOrderStereotype.class, transformer);
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert !animalStereotype.isBeanNameDefaulted();
      assert !animalStereotype.isPolicy();
   }
   
   @Test
   public void testRequestScopedAnimalStereotype()
   {
      StereotypeModel<RequestScopedAnimalStereotype> animalStereotype = new StereotypeModel<RequestScopedAnimalStereotype>(RequestScopedAnimalStereotype.class, transformer);
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert !animalStereotype.isBeanNameDefaulted();
      assert !animalStereotype.isPolicy();
   }
      
}
