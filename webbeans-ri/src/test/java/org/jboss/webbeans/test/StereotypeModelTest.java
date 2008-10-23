package org.jboss.webbeans.test;

import java.util.Arrays;

import javax.webbeans.RequestScoped;

import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.test.annotations.AnimalOrderStereotype;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.HornedMammalStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.annotations.broken.StereotypeWithBindingTypes;
import org.jboss.webbeans.test.annotations.broken.StereotypeWithNonEmptyNamed;
import org.jboss.webbeans.test.annotations.broken.StereotypeWithTooManyDeploymentTypes;
import org.jboss.webbeans.test.annotations.broken.StereotypeWithTooManyScopeTypes;
import org.jboss.webbeans.test.components.Animal;
import org.jboss.webbeans.test.components.Order;
import org.testng.annotations.Test;

@SpecVersion("20080925")
public class StereotypeModelTest
{
	
   @Test @SpecAssertion(section="2.7.1")
   public void testHasCorrectTarget()
   {
	   assert false;
   }
   
   @Test @SpecAssertion(section="2.7.1")
   public void testHasCorrectRetention()
   {
	   assert false;
   }
   
   @Test @SpecAssertion(section="2.7.1")
   public void testHasStereotypeAnnotation()
   {
	   assert false;
   }
   
   @Test
   public void testAnimalStereotype()
   {
      StereotypeModel<AnimalStereotype> animalStereotype = new StereotypeModel<AnimalStereotype>(new SimpleAnnotatedType<AnimalStereotype>(AnimalStereotype.class));
      assert animalStereotype.getDefaultScopeType().annotationType().equals(RequestScoped.class);
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert animalStereotype.getRequiredTypes().size() == 1;
      assert animalStereotype.getRequiredTypes().contains(Animal.class);
      assert animalStereotype.getSupportedScopes().size() == 0;
      assert !animalStereotype.isComponentNameDefaulted();
      assert animalStereotype.getDefaultDeploymentType() == null;
   }
   
   @Test
   public void testAnimalOrderStereotype()
   {
      StereotypeModel<AnimalOrderStereotype> animalStereotype = new StereotypeModel<AnimalOrderStereotype>(new SimpleAnnotatedType<AnimalOrderStereotype>(AnimalOrderStereotype.class));
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert animalStereotype.getRequiredTypes().size() == 2;
      Class<?> [] requiredTypes = {Animal.class, Order.class};
      assert animalStereotype.getRequiredTypes().containsAll(Arrays.asList(requiredTypes));
      assert animalStereotype.getSupportedScopes().size() == 0;
      assert !animalStereotype.isComponentNameDefaulted();
      assert animalStereotype.getDefaultDeploymentType() == null;
   }
   
   @Test
   public void testRequestScopedAnimalStereotype()
   {
      StereotypeModel<RequestScopedAnimalStereotype> animalStereotype = new StereotypeModel<RequestScopedAnimalStereotype>(new SimpleAnnotatedType<RequestScopedAnimalStereotype>(RequestScopedAnimalStereotype.class));
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert animalStereotype.getRequiredTypes().size() == 1;
      assert Animal.class.equals(animalStereotype.getRequiredTypes().iterator().next());
      assert animalStereotype.getSupportedScopes().size() == 1;
      assert animalStereotype.getSupportedScopes().contains(RequestScoped.class);
      assert !animalStereotype.isComponentNameDefaulted();
      assert animalStereotype.getDefaultDeploymentType() == null;
   }
   
   @Test @SpecAssertion(section="2.7.1")
   public void testStereotypeWithScopeType()
   {
	   StereotypeModel<AnimalStereotype> animalStereotype = new StereotypeModel<AnimalStereotype>(new SimpleAnnotatedType<AnimalStereotype>(AnimalStereotype.class));
	   assert animalStereotype.getDefaultScopeType().annotationType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.7.1")
   public void testStereotypeWithoutScopeType()
   {
	   StereotypeModel<HornedMammalStereotype> animalStereotype = new StereotypeModel<HornedMammalStereotype>(new SimpleAnnotatedType<HornedMammalStereotype>(HornedMammalStereotype.class));
	   assert animalStereotype.getDefaultScopeType() == null;
   }
   
   @Test @SpecAssertion(section="2.7.1")
   public void testStereotypeWithoutInterceptors()
   {
      StereotypeModel<AnimalStereotype> animalStereotype = new StereotypeModel<AnimalStereotype>(new SimpleAnnotatedType<AnimalStereotype>(AnimalStereotype.class));
      assert animalStereotype.getInterceptorBindings().size() == 0;
   }
   
   @Test @SpecAssertion(section="2.7.1")
   public void testStereotypeWithInterceptors()
   {
      assert false;
   }
   
   @Test(expectedExceptions=RuntimeException.class) @SpecAssertion(section="2.7.1")
   public void testStereotypeWithTooManyScopeTypes()
   {
      new StereotypeModel<StereotypeWithTooManyScopeTypes>(new SimpleAnnotatedType<StereotypeWithTooManyScopeTypes>(StereotypeWithTooManyScopeTypes.class));
   }
   
   @Test(expectedExceptions=RuntimeException.class) @SpecAssertion(section="2.7.1")
   public void testStereotypeWithTooManyDeploymentTypes()
   {
      new StereotypeModel<StereotypeWithTooManyDeploymentTypes>(new SimpleAnnotatedType<StereotypeWithTooManyDeploymentTypes>(StereotypeWithTooManyDeploymentTypes.class));
   }
   
   @Test(expectedExceptions=RuntimeException.class) @SpecAssertion(section="2.7.1")
   public void testStereotypeWithNonEmptyNamed()
   {
      new StereotypeModel<StereotypeWithNonEmptyNamed>(new SimpleAnnotatedType<StereotypeWithNonEmptyNamed>(StereotypeWithNonEmptyNamed.class));
   }
   
   @Test(expectedExceptions=RuntimeException.class) @SpecAssertion(section="2.7.1")
   public void testStereotypeWithBindingTypes()
   {
      new StereotypeModel<StereotypeWithBindingTypes>(new SimpleAnnotatedType<StereotypeWithBindingTypes>(StereotypeWithBindingTypes.class));
   }
   
}
