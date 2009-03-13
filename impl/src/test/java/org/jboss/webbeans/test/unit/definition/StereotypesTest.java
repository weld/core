package org.jboss.webbeans.test.unit.definition;

import java.util.Arrays;

import javax.context.RequestScoped;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.metadata.StereotypeModel;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class StereotypesTest extends AbstractWebBeansTest
{
	
   @Test
   public void testAnimalStereotype()
   {
      StereotypeModel<AnimalStereotype> animalStereotype = new StereotypeModel<AnimalStereotype>(AnimalStereotype.class);
      assert animalStereotype.getDefaultScopeType().annotationType().equals(RequestScoped.class);
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert animalStereotype.getRequiredTypes().size() == 1;
      assert animalStereotype.getRequiredTypes().contains(Animal.class);
      assert animalStereotype.getSupportedScopes().size() == 0;
      assert !animalStereotype.isBeanNameDefaulted();
      assert animalStereotype.getDefaultDeploymentType() == null;
   }
   
   @Test
   public void testAnimalOrderStereotype()
   {
      StereotypeModel<AnimalOrderStereotype> animalStereotype = new StereotypeModel<AnimalOrderStereotype>(AnimalOrderStereotype.class);
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert animalStereotype.getRequiredTypes().size() == 2;
      Class<?> [] requiredTypes = {Animal.class, Order.class};
      assert animalStereotype.getRequiredTypes().containsAll(Arrays.asList(requiredTypes));
      assert animalStereotype.getSupportedScopes().size() == 0;
      assert !animalStereotype.isBeanNameDefaulted();
      assert animalStereotype.getDefaultDeploymentType() == null;
   }
   
   @Test
   public void testRequestScopedAnimalStereotype()
   {
      StereotypeModel<RequestScopedAnimalStereotype> animalStereotype = new StereotypeModel<RequestScopedAnimalStereotype>(RequestScopedAnimalStereotype.class);
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert animalStereotype.getRequiredTypes().size() == 1;
      assert Animal.class.equals(animalStereotype.getRequiredTypes().iterator().next());
      assert animalStereotype.getSupportedScopes().size() == 1;
      assert animalStereotype.getSupportedScopes().contains(RequestScoped.class);
      assert !animalStereotype.isBeanNameDefaulted();
      assert animalStereotype.getDefaultDeploymentType() == null;
   }
      
}
