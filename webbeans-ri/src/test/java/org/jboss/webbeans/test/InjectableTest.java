package org.jboss.webbeans.test;

import java.util.HashSet;
import java.util.Set;

import javax.webbeans.AnnotationLiteral;
import javax.webbeans.Current;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.ResolutionManager;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.annotations.Chunky;
import org.jboss.webbeans.test.annotations.Whitefish;
import org.jboss.webbeans.test.components.Animal;
import org.jboss.webbeans.test.components.Cod;
import org.jboss.webbeans.test.components.FishFarm;
import org.jboss.webbeans.test.components.Haddock;
import org.jboss.webbeans.test.components.Salmon;
import org.jboss.webbeans.test.components.ScottishFish;
import org.jboss.webbeans.test.components.SeaBass;
import org.jboss.webbeans.test.components.Sole;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

public class InjectableTest extends AbstractTest
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
      Bean<Tuna> tunaBean = new BeanImpl<Tuna>(new SimpleComponentModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), getEmptyAnnotatedItem(Tuna.class), super.manager), manager);
      Set<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add(tunaBean);
      Set<Bean<?>> possibleTargets = tunaField.getPossibleBeans(beans);
      assert possibleTargets.size() == 1;
      assert possibleTargets.contains(tunaBean);
   }
   
   @Test
   public void testOneBindingType() throws Exception
   {
      InjectableField<ScottishFish> scottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("scottishFish"));
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleComponentModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleComponentModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleComponentModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      Set<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add(codBean);
      beans.add(salmonBean);
      beans.add(soleBean);
      Set<Bean<?>> possibleTargets = scottishFishField.getPossibleBeans(beans);
      assert possibleTargets.size() == 2;
      assert possibleTargets.contains(codBean);
      assert possibleTargets.contains(soleBean);
   }
   
   @Test
   public void testABindingType() throws Exception
   {
      InjectableField<Animal> whiteChunkyFishField = new InjectableField<Animal>(FishFarm.class.getDeclaredField("whiteChunkyFish"));
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleComponentModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleComponentModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleComponentModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      Set<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add(codBean);
      beans.add(salmonBean);
      beans.add(soleBean);
      Set<Bean<?>> possibleTargets = whiteChunkyFishField.getPossibleBeans(beans);
      assert possibleTargets.size() == 1;
      assert possibleTargets.contains(codBean);
   }
   
   @Test
   public void testMultipleApiTypeWithCurrent() throws Exception
   {
      InjectableField<Animal> animalField = new InjectableField<Animal>(FishFarm.class.getDeclaredField("animal"));
      Bean<SeaBass> seaBassBean = new BeanImpl<SeaBass>(new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedItem(SeaBass.class), super.manager), manager);
      Bean<Haddock> haddockBean = new BeanImpl<Haddock>(new SimpleComponentModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), getEmptyAnnotatedItem(Haddock.class), super.manager), manager);
      Set<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add(seaBassBean);
      beans.add(haddockBean);
      Set<Bean<?>> possibleTargets = animalField.getPossibleBeans(beans);
      assert possibleTargets.size() == 2;
      assert possibleTargets.contains(seaBassBean);
      assert possibleTargets.contains(haddockBean);
   }
   
   @Test
   public void testResolveByType() throws Exception
   {
      InjectableField<Animal> whiteChunkyFishField = new InjectableField<Animal>(FishFarm.class.getDeclaredField("whiteChunkyFish"));
      InjectableField<Animal> animalField = new InjectableField<Animal>(FishFarm.class.getDeclaredField("animal"));
      InjectableField<ScottishFish> scottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("scottishFish"));
      InjectableField<Tuna> tunaField = new InjectableField<Tuna>(FishFarm.class.getDeclaredField("tuna"));
      
      Bean<Tuna> tunaBean = new BeanImpl<Tuna>(new SimpleComponentModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), getEmptyAnnotatedItem(Tuna.class), super.manager), manager);
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleComponentModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleComponentModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleComponentModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      Bean<SeaBass> seaBassBean = new BeanImpl<SeaBass>(new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedItem(SeaBass.class), super.manager), manager);
      Bean<Haddock> haddockBean = new BeanImpl<Haddock>(new SimpleComponentModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), getEmptyAnnotatedItem(Haddock.class), super.manager), manager);
      
      manager.addBean(tunaBean);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      manager.addBean(haddockBean);
      manager.addBean(seaBassBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteChunkyFishField);
      resolutionManager.addInjectionPoint(animalField);
      resolutionManager.addInjectionPoint(scottishFishField);
      resolutionManager.addInjectionPoint(tunaField);
      
      resolutionManager.registerInjectionPoints();
      
      System.out.println("injection points registered");
      
      assert manager.resolveByType(Tuna.class, new CurrentAnnotationLiteral()).size() == 1;
      assert manager.resolveByType(Tuna.class, new CurrentAnnotationLiteral()).contains(tunaBean);
      
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).size() == 3;
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).contains(salmonBean);
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).contains(seaBassBean);
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).contains(haddockBean);
      
      assert manager.resolveByType(Animal.class, new AnnotationLiteral<Chunky>() {}, new AnnotationLiteral<Whitefish>() {}).size() == 1;
      assert manager.resolveByType(Animal.class, new AnnotationLiteral<Chunky>() {}, new AnnotationLiteral<Whitefish>() {}).contains(codBean);
      
      assert manager.resolveByType(ScottishFish.class, new AnnotationLiteral<Whitefish>() {}).size() == 2;
      assert manager.resolveByType(ScottishFish.class, new AnnotationLiteral<Whitefish>() {}).contains(codBean);
      assert manager.resolveByType(ScottishFish.class, new AnnotationLiteral<Whitefish>() {}).contains(soleBean);
      
   }
      
}
