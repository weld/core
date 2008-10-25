package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedItem;

import java.util.HashSet;
import java.util.Set;

import javax.webbeans.AnnotationLiteral;
import javax.webbeans.Current;
import javax.webbeans.TypeLiteral;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.ResolutionManager;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.Whitefish;
import org.jboss.webbeans.test.beans.Animal;
import org.jboss.webbeans.test.beans.Cod;
import org.jboss.webbeans.test.beans.Farmer;
import org.jboss.webbeans.test.beans.FishFarm;
import org.jboss.webbeans.test.beans.Haddock;
import org.jboss.webbeans.test.beans.Plaice;
import org.jboss.webbeans.test.beans.Salmon;
import org.jboss.webbeans.test.beans.ScottishFish;
import org.jboss.webbeans.test.beans.ScottishFishFarmer;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.beans.Sole;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.bindings.ChunkyAnnotationLiteral;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

public class TypeSafeResolutionTest extends AbstractTest
{

   @SuppressWarnings("unchecked")
   @Test
   public void testInjectableField() throws Exception
   {
      InjectableField<Tuna> tuna = new InjectableField<Tuna>(FishFarm.class.getDeclaredField("tuna"));
      assert tuna.getAnnotatedItem().getType().isAssignableFrom(Tuna.class);
      assert tuna.getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(tuna.getBindingTypes(), Current.class);
      assert tuna.getType().isAssignableFrom(Tuna.class);
   }
   
   @Test
   public void testSingleApiTypeWithCurrent() throws Exception
   {
      InjectableField<Tuna> tunaField = new InjectableField<Tuna>(FishFarm.class.getDeclaredField("tuna"));
      Bean<Tuna> tunaBean = new BeanImpl<Tuna>(new SimpleBeanModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), getEmptyAnnotatedItem(Tuna.class), super.manager), manager);
      Set<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add(tunaBean);
      Set<Bean<?>> possibleTargets = tunaField.getMatchingBeans(beans);
      assert possibleTargets.size() == 1;
      assert possibleTargets.contains(tunaBean);
   }
   
   @Test
   public void testOneBindingType() throws Exception
   {
      InjectableField<ScottishFish> whiteScottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleBeanModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleBeanModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      Set<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add(codBean);
      beans.add(salmonBean);
      beans.add(soleBean);
      Set<Bean<?>> possibleTargets = whiteScottishFishField.getMatchingBeans(beans);
      assert possibleTargets.size() == 2;
      assert possibleTargets.contains(codBean);
      assert possibleTargets.contains(soleBean);
   }
   
   @Test
   public void testABindingType() throws Exception
   {
      InjectableField<Animal> whiteChunkyFishField = new InjectableField<Animal>(FishFarm.class.getDeclaredField("realChunkyWhiteFish"));
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleBeanModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleBeanModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      Set<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add(codBean);
      beans.add(salmonBean);
      beans.add(soleBean);
      Set<Bean<?>> possibleTargets = whiteChunkyFishField.getMatchingBeans(beans);
      assert possibleTargets.size() == 1;
      assert possibleTargets.contains(codBean);
   }
   
   @Test
   public void testMultipleApiTypeWithCurrent() throws Exception
   {
      InjectableField<Animal> animalField = new InjectableField<Animal>(FishFarm.class.getDeclaredField("animal"));
      Bean<SeaBass> seaBassBean = new BeanImpl<SeaBass>(new SimpleBeanModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedItem(SeaBass.class), super.manager), manager);
      Bean<Haddock> haddockBean = new BeanImpl<Haddock>(new SimpleBeanModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), getEmptyAnnotatedItem(Haddock.class), super.manager), manager);
      Set<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add(seaBassBean);
      beans.add(haddockBean);
      Set<Bean<?>> possibleTargets = animalField.getMatchingBeans(beans);
      assert possibleTargets.size() == 2;
      assert possibleTargets.contains(seaBassBean);
      assert possibleTargets.contains(haddockBean);
   }
   
   @Test
   public void testResolveByType() throws Exception
   {
      InjectableField<Animal> realChunkyWhiteFishField = new InjectableField<Animal>(FishFarm.class.getDeclaredField("realChunkyWhiteFish"));
      InjectableField<Animal> animalField = new InjectableField<Animal>(FishFarm.class.getDeclaredField("animal"));
      InjectableField<ScottishFish> scottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      InjectableField<Tuna> tunaField = new InjectableField<Tuna>(FishFarm.class.getDeclaredField("tuna"));
      
      Bean<Tuna> tunaBean = new BeanImpl<Tuna>(new SimpleBeanModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), getEmptyAnnotatedItem(Tuna.class), super.manager), manager);
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleBeanModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleBeanModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      Bean<SeaBass> seaBassBean = new BeanImpl<SeaBass>(new SimpleBeanModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedItem(SeaBass.class), super.manager), manager);
      Bean<Haddock> haddockBean = new BeanImpl<Haddock>(new SimpleBeanModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), getEmptyAnnotatedItem(Haddock.class), super.manager), manager);
      
      manager.addBean(tunaBean);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      manager.addBean(haddockBean);
      manager.addBean(seaBassBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(realChunkyWhiteFishField);
      resolutionManager.addInjectionPoint(animalField);
      resolutionManager.addInjectionPoint(scottishFishField);
      resolutionManager.addInjectionPoint(tunaField);
      
      assert manager.resolveByType(Tuna.class, new CurrentAnnotationLiteral()).size() == 1;
      assert manager.resolveByType(Tuna.class, new CurrentAnnotationLiteral()).contains(tunaBean);
      
      assert manager.resolveByType(Tuna.class).size() == 1;
      assert manager.resolveByType(Tuna.class).contains(tunaBean);
      
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).size() == 3;
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).contains(salmonBean);
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).contains(seaBassBean);
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).contains(haddockBean);
      
      assert manager.resolveByType(Animal.class, new ChunkyAnnotationLiteral() {

         public boolean realChunky()
         {
            return true;
         }
         
      }, new AnnotationLiteral<Whitefish>() {}).size() == 1;
      assert manager.resolveByType(Animal.class, new ChunkyAnnotationLiteral() {

         public boolean realChunky()
         {
            return true;
         }
         
      }, new AnnotationLiteral<Whitefish>() {}).contains(codBean);
      
      assert manager.resolveByType(ScottishFish.class, new AnnotationLiteral<Whitefish>() {}).size() == 2;
      assert manager.resolveByType(ScottishFish.class, new AnnotationLiteral<Whitefish>() {}).contains(codBean);
      assert manager.resolveByType(ScottishFish.class, new AnnotationLiteral<Whitefish>() {}).contains(soleBean);
      
   }
   
   @Test
   public void testResolveByTypeWithTypeParameter() throws Exception
   {
      InjectableField<Farmer<ScottishFish>> scottishFishFarmerField = new InjectableField<Farmer<ScottishFish>>(FishFarm.class.getDeclaredField("scottishFishFarmer"));
      
      Bean<ScottishFishFarmer> scottishFishFarmerBean = new BeanImpl<ScottishFishFarmer>(new SimpleBeanModel<ScottishFishFarmer>(new SimpleAnnotatedType<ScottishFishFarmer>(ScottishFishFarmer.class), getEmptyAnnotatedItem(ScottishFishFarmer.class), super.manager), manager);
      Bean<Farmer> farmerBean = new BeanImpl<Farmer>(new SimpleBeanModel<Farmer>(new SimpleAnnotatedType<Farmer>(Farmer.class), getEmptyAnnotatedItem(Farmer.class), super.manager), manager);
      
      manager.addBean(scottishFishFarmerBean);
      manager.addBean(farmerBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(scottishFishFarmerField);
      
      assert manager.resolveByType(new TypeLiteral<Farmer<ScottishFish>>(){}).size() == 1;
      assert manager.resolveByType(new TypeLiteral<Farmer<ScottishFish>>(){}).contains(scottishFishFarmerBean);
   }
   
   @Test
   public void testOnlyHighestEnabledPreecedenceWebBeansResolved() throws Exception
   {
      InjectableField<Animal> whiteFishField = new InjectableField<Animal>(FishFarm.class.getDeclaredField("whiteFish"));
      
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleBeanModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      Bean<Plaice> plaiceBean = new BeanImpl<Plaice>(new SimpleBeanModel<Plaice>(new SimpleAnnotatedType<Plaice>(Plaice.class), getEmptyAnnotatedItem(Plaice.class), super.manager), manager);
      
      
      manager.addBean(plaiceBean);
      manager.addBean(codBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteFishField);

      assert manager.resolveByType(Animal.class, new AnnotationLiteral<Whitefish>() {}).size() == 1;
      assert manager.resolveByType(Animal.class, new AnnotationLiteral<Whitefish>() {}).contains(plaiceBean);
      
   }
      
}
