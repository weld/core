package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Decorator;
import javax.webbeans.Interceptor;
import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.bootstrap.Bootstrap;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.annotations.HornedMammalStereotype;
import org.jboss.webbeans.test.annotations.MammalStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.mock.MockBootstrap;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.BeforeMethod;

public class AbstractTest
{
   
   protected MockManagerImpl manager;
   protected Bootstrap bootstrap;

   
   @BeforeMethod
   public final void before()
   {
      manager = new MockManagerImpl();
      bootstrap = new MockBootstrap(manager);
      init();
   }
   
   protected void init()
   {
      addStereotypes();
      addEnabledDeploymentTypes();
   }
   
   protected void addEnabledDeploymentTypes()
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(Production.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      enabledDeploymentTypes.add(HornedAnimalDeploymentType.class);
      manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
   }
   
   protected void addStereotypes()
   {
      manager.getModelManager().addStereotype(new StereotypeModel<Decorator>(Decorator.class));
      manager.getModelManager().addStereotype(new StereotypeModel<Interceptor>(Interceptor.class));
      manager.getModelManager().addStereotype(new StereotypeModel<AnimalStereotype>(AnimalStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<HornedMammalStereotype>(HornedMammalStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<MammalStereotype>(MammalStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<FishStereotype>(FishStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<RiverFishStereotype>(RiverFishStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<RequestScopedAnimalStereotype>(RequestScopedAnimalStereotype.class));
   }

}
