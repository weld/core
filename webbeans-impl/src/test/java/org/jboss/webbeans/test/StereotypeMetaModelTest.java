package org.jboss.webbeans.test;

import java.util.Arrays;

import javax.webbeans.Component;
import javax.webbeans.Model;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.StereotypeMetaModel;
import org.jboss.webbeans.test.components.Animal;
import org.jboss.webbeans.test.components.AnimalOrderStereotype;
import org.jboss.webbeans.test.components.AnimalStereotype;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.test.components.RequestScopedAnimalStereotype;
import org.jboss.webbeans.util.MutableAnnotatedWebBean;
import org.junit.Test;

public class StereotypeMetaModelTest
{
   
   @Test
   public void testComponentStereotype()
   {
      StereotypeMetaModel componentStereotype = new StereotypeMetaModel(new MutableAnnotatedWebBean(Component.class));
      
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
      StereotypeMetaModel modelStereotype = new StereotypeMetaModel(new MutableAnnotatedWebBean(Model.class));
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
      StereotypeMetaModel animalStereotype = new StereotypeMetaModel(new MutableAnnotatedWebBean(AnimalStereotype.class));
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert animalStereotype.getRequiredTypes().size() == 1;
      assert Animal.class.equals(animalStereotype.getRequiredTypes().iterator().next());
      assert animalStereotype.getSupportedScopes().size() == 0;
      assert !animalStereotype.isComponentNameDefaulted();
      assert animalStereotype.getDefaultDeploymentType() == null;
   }
   
   @Test
   public void testAnimalOrderStereotype()
   {
      StereotypeMetaModel animalStereotype = new StereotypeMetaModel(new MutableAnnotatedWebBean(AnimalOrderStereotype.class));
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
      StereotypeMetaModel animalStereotype = new StereotypeMetaModel(new MutableAnnotatedWebBean(RequestScopedAnimalStereotype.class));
      assert animalStereotype.getDefaultScopeType() == null;
      assert animalStereotype.getInterceptorBindings().size() == 0;
      assert animalStereotype.getRequiredTypes().size() == 1;
      assert Animal.class.equals(animalStereotype.getRequiredTypes().iterator().next());
      assert animalStereotype.getSupportedScopes().size() == 1;
      assert RequestScoped.class.equals(animalStereotype.getSupportedScopes().iterator().next());
      assert !animalStereotype.isComponentNameDefaulted();
      assert animalStereotype.getDefaultDeploymentType() == null;
   }
   
}
