package org.jboss.webbeans.test.unit;

import java.util.Arrays;

import javax.webbeans.RequestScoped;

import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.annotations.AnimalOrderStereotype;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.beans.Animal;
import org.jboss.webbeans.test.beans.Order;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class StereotypesTest extends AbstractTest
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
