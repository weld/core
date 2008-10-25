package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedItem;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.AnnotationLiteral;
import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.ResolutionManager;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.Whitefish;
import org.jboss.webbeans.test.components.Cod;
import org.jboss.webbeans.test.components.FishFarm;
import org.jboss.webbeans.test.components.Plaice;
import org.jboss.webbeans.test.components.Salmon;
import org.jboss.webbeans.test.components.ScottishFish;
import org.jboss.webbeans.test.components.Sole;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.test.components.broken.PlaiceFarm;
import org.testng.annotations.Test;

public class InstantiationByTypeTest extends AbstractTest
{
   
   @Test(expectedExceptions=AmbiguousDependencyException.class)
   public void testAmbiguousDependencies() throws Exception
   {
      InjectableField<ScottishFish> whiteScottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      Bean<Cod> plaiceBean = new BeanImpl<Cod>(new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleBeanModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleBeanModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      manager.addBean(plaiceBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      
      manager.getInstanceByType(ScottishFish.class, new AnnotationLiteral<Whitefish>(){});
   }
   
   @Test(expectedExceptions=UnsatisfiedDependencyException.class)
   public void testUnsatisfiedDependencies() throws Exception
   {
      InjectableField<ScottishFish> whiteScottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      Bean<Cod> plaiceBean = new BeanImpl<Cod>(new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleBeanModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleBeanModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      manager.addBean(plaiceBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      
      manager.getInstanceByType(Tuna.class, new CurrentAnnotationLiteral());
   }
   
   @Test(expectedExceptions=UnproxyableDependencyException.class)
   public void testUnproxyableDependencies() throws Exception
   {
      InjectableField<Plaice> plaiceField = new InjectableField<Plaice>(PlaiceFarm.class.getDeclaredField("plaice"));
      Bean<Plaice> plaiceBean = new BeanImpl<Plaice>(new SimpleBeanModel<Plaice>(new SimpleAnnotatedType<Plaice>(Plaice.class), getEmptyAnnotatedItem(Plaice.class), super.manager), manager);
      manager.addBean(plaiceBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(plaiceField);
      
      manager.getInstanceByType(Plaice.class, new AnnotationLiteral<Whitefish>(){});
   }
   
}
