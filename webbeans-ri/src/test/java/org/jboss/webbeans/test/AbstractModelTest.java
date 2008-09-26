package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.bindings.StandardBinding;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.HornedMammalStereotype;
import org.jboss.webbeans.test.annotations.MammalStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeBinding;
import org.jboss.webbeans.test.bindings.HornedAnimalDeploymentTypeBinding;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.testng.annotations.BeforeMethod;

public class AbstractModelTest
{
   
   protected ContainerImpl container;
   
   protected AnnotatedType<?> emptyAnnotatedItem;
   
   @BeforeMethod
   public void before()
   {
      emptyAnnotatedItem = new SimpleAnnotatedType<Object>(null, new HashMap<Class<? extends Annotation>, Annotation>());
      
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardBinding());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeBinding());
      enabledDeploymentTypes.add(new HornedAnimalDeploymentTypeBinding());
      container = new MockContainerImpl(enabledDeploymentTypes);
      
      initStereotypes(container);
   }
   
   private void initStereotypes(ContainerImpl container)
   {
      container.getModelManager().addStereotype(new StereotypeModel<AnimalStereotype>(new SimpleAnnotatedType<AnimalStereotype>(AnimalStereotype.class)));
      container.getModelManager().addStereotype(new StereotypeModel<HornedMammalStereotype>(new SimpleAnnotatedType<HornedMammalStereotype>(HornedMammalStereotype.class)));
      container.getModelManager().addStereotype(new StereotypeModel<MammalStereotype>(new SimpleAnnotatedType<MammalStereotype>(MammalStereotype.class)));
      container.getModelManager().addStereotype(new StereotypeModel<FishStereotype>(new SimpleAnnotatedType<FishStereotype>(FishStereotype.class)));
      container.getModelManager().addStereotype(new StereotypeModel<RiverFishStereotype>(new SimpleAnnotatedType<RiverFishStereotype>(RiverFishStereotype.class)));
      container.getModelManager().addStereotype(new StereotypeModel<RequestScopedAnimalStereotype>(new SimpleAnnotatedType<RequestScopedAnimalStereotype>(RequestScopedAnimalStereotype.class)));
   }

}
