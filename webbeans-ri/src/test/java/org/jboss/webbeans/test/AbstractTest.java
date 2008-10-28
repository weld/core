package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.contexts.ApplicationContext;
import org.jboss.webbeans.contexts.DependentContext;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.annotations.HornedMammalStereotype;
import org.jboss.webbeans.test.annotations.MammalStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.BeforeMethod;

public class AbstractTest
{
   
   protected MockManagerImpl manager;

   
   @BeforeMethod
   public final void before()
   {
      manager = new MockManagerImpl();
      init();
   }
   
   protected void init()
   {
      addStereotypes();
      addBuiltInContexts();
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
      manager.getModelManager().addStereotype(new StereotypeModel<AnimalStereotype>(AnimalStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<HornedMammalStereotype>(HornedMammalStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<MammalStereotype>(MammalStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<FishStereotype>(FishStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<RiverFishStereotype>(RiverFishStereotype.class));
      manager.getModelManager().addStereotype(new StereotypeModel<RequestScopedAnimalStereotype>(RequestScopedAnimalStereotype.class));
   }
   
   protected void addBuiltInContexts()
   {
      manager.addContext(new DependentContext());
      manager.addContext(new RequestContext());
      manager.addContext(new SessionContext());
      manager.addContext(new ApplicationContext());
   }

}
