package org.jboss.webbeans.test;

import java.util.Arrays;

import javax.webbeans.Component;
import javax.webbeans.Model;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.StereotypeMetaModel;
import org.jboss.webbeans.test.annotations.AnimalOrderStereotype;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.annotations.StereotypeWithBindingTypes;
import org.jboss.webbeans.test.annotations.StereotypeWithNonEmptyNamed;
import org.jboss.webbeans.test.annotations.StereotypeWithTooManyDeploymentTypes;
import org.jboss.webbeans.test.annotations.StereotypeWithTooManyScopeTypes;
import org.jboss.webbeans.test.components.Animal;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.util.ClassAnnotatedItem;
import org.junit.Test;

public class StereotypeMetaModelTest
{
   
   @Test
   public void testComponentStereotype()
   {
      StereotypeMetaModel componentStereotype = new StereotypeMetaModel(new ClassAnnotatedItem(Component.class));
      
      assert Production.class.equals(componentStereotype.getDefaultDeploymentType().annotationType());
      assert componentStereotype.getDefaultScopeType() == null;
      assert componentStereotype.getInterceptorBindings().size() == 0;
      assert componentStereotype.getRequiredTypes().size() == 0;
      assert componentStereotype.getSupportedScopes().size() == 0;
      assert !componentStereotype.isComponentNameDefaulted();
   }
   
   @Test
   public void testModelStereotype()
   {
      StereotypeMetaModel modelStereotype = new StereotypeMetaModel(new ClassAnnotatedItem(Model.class));
      assert Production.class.equals(modelStereotype.getDefaultDeploymentType().annotationType());
      assert RequestScoped.class.equals(modelStereotype.getDefaultScopeType().annotationType());
      assert modelStereotype.isComponentNameDefaulted();
      assert modelStereotype.getInterceptorBindings().size() == 0;
      assert modelStereotype.getRequiredTypes().size() == 0;
      assert modelStereotype.getSupportedScopes().size() == 0;
   }
   
   @Test
   public void testAnimalStereotype()
   {
      StereotypeMetaModel animalStereotype = new StereotypeMetaModel(new ClassAnnotatedItem(AnimalStereotype.class));
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
      StereotypeMetaModel animalStereotype = new StereotypeMetaModel(new ClassAnnotatedItem(AnimalOrderStereotype.class));
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
      StereotypeMetaModel animalStereotype = new StereotypeMetaModel(new ClassAnnotatedItem(RequestScopedAnimalStereotype.class));
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert animalStereotype.getRequiredTypes().size() == 1;
      assert Animal.class.equals(animalStereotype.getRequiredTypes().iterator().next());
      assert animalStereotype.getSupportedScopes().size() == 1;
      assert animalStereotype.getSupportedScopes().contains(RequestScoped.class);
      assert !animalStereotype.isComponentNameDefaulted();
      assert animalStereotype.getDefaultDeploymentType() == null;
   }
   
   @Test
   public void testStereotypeWithTooManyScopeTypes()
   {
      boolean exception = false;
      try
      {
         new StereotypeMetaModel(new ClassAnnotatedItem(StereotypeWithTooManyScopeTypes.class));
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testStereotypeWithTooManyDeploymentTypes()
   {
      boolean exception = false;
      try
      {
         new StereotypeMetaModel(new ClassAnnotatedItem(StereotypeWithTooManyDeploymentTypes.class));
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testStereotypeWithNonEmptyNamed()
   {
      boolean exception = false;
      try
      {
         new StereotypeMetaModel(new ClassAnnotatedItem(StereotypeWithNonEmptyNamed.class));
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testStereotypeWithBindingTypes()
   {
      boolean exception = false;
      try
      {
         new StereotypeMetaModel(new ClassAnnotatedItem(StereotypeWithBindingTypes.class));
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
}
