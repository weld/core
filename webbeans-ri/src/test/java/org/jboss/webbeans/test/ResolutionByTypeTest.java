package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createProducerMethodBean;
import static org.jboss.webbeans.test.util.Util.createSimpleBean;

import java.lang.reflect.Method;
import java.util.Set;

import javax.webbeans.AnnotationLiteral;
import javax.webbeans.DefinitionException;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.TypeLiteral;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ResolutionManager;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedFieldImpl;
import org.jboss.webbeans.test.annotations.Expensive;
import org.jboss.webbeans.test.annotations.Whitefish;
import org.jboss.webbeans.test.beans.Animal;
import org.jboss.webbeans.test.beans.AnimalFarmer;
import org.jboss.webbeans.test.beans.Cod;
import org.jboss.webbeans.test.beans.Farmer;
import org.jboss.webbeans.test.beans.FishFarm;
import org.jboss.webbeans.test.beans.Haddock;
import org.jboss.webbeans.test.beans.Halibut;
import org.jboss.webbeans.test.beans.Plaice;
import org.jboss.webbeans.test.beans.RoundWhitefish;
import org.jboss.webbeans.test.beans.Salmon;
import org.jboss.webbeans.test.beans.ScottishFish;
import org.jboss.webbeans.test.beans.ScottishFishFarmer;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.beans.Sole;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.BindingTypeWithBindingAnnotationMemberAnnotationLiteral;
import org.jboss.webbeans.test.bindings.BindingTypeWithBindingArrayTypeMemberAnnotationLiteral;
import org.jboss.webbeans.test.bindings.ChunkyAnnotationLiteral;
import org.jboss.webbeans.test.bindings.ExpensiveAnnotationLiteral;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ResolutionByTypeTest extends AbstractTest
{
   
   private AnnotatedClass<FishFarm> fishFarmClass = new AnnotatedClassImpl<FishFarm>(FishFarm.class);

   @Test(groups="resolution")
   public void testAnnotatedField() throws Exception
   {
      AnnotatedField<Tuna> tuna = new AnnotatedFieldImpl<Tuna>(FishFarm.class.getDeclaredField("tuna"), fishFarmClass);
      assert tuna.getType().isAssignableFrom(Tuna.class);
      assert tuna.getBindingTypes().size() == 1;
      assert tuna.getBindingTypes().contains(new CurrentAnnotationLiteral());
      assert tuna.getType().isAssignableFrom(Tuna.class);
   }
   
   @Test(groups="resolution") @SpecAssertion(section="4.9.2")
   public void testSingleApiTypeWithCurrent() throws Exception
   {
      AnnotatedField<Tuna> tunaField = new AnnotatedFieldImpl<Tuna>(FishFarm.class.getDeclaredField("tuna"), fishFarmClass);
      Bean<Tuna> tunaBean = createSimpleBean(Tuna.class, manager);
      manager.addBean(tunaBean);
      Set<Bean<Tuna>> possibleTargets = manager.getResolutionManager().get(tunaField);
      assert possibleTargets.size() == 1;
      assert possibleTargets.contains(tunaBean);
   }
   
   @Test(groups="resolution", expectedExceptions=DuplicateBindingTypeException.class) @SpecAssertion(section="4.9.2")
   public void testDuplicateBindingTypesUsed()
   {
      manager.resolveByType(Tuna.class, new CurrentAnnotationLiteral(), new CurrentAnnotationLiteral());
   }
   
   @Test(groups="resolution", expectedExceptions=IllegalArgumentException.class) @SpecAssertion(section="4.9.2")
   public void testNonBindingTypeUsed()
   {
      manager.resolveByType(Tuna.class, new AnotherDeploymentTypeAnnotationLiteral());
   }
   
   @Test
   public void testOneBindingType() throws Exception
   {
      AnnotatedField<ScottishFish> whiteScottishFishField = new AnnotatedFieldImpl<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"), fishFarmClass);
      Bean<Cod> codBean = createSimpleBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleBean(Sole.class, manager);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      Set<Bean<ScottishFish>> possibleTargets = manager.getResolutionManager().get(whiteScottishFishField);
      assert possibleTargets.size() == 2;
      assert possibleTargets.contains(codBean);
      assert possibleTargets.contains(soleBean);
   }
   
   @Test
   public void testABindingType() throws Exception
   {
      AnnotatedField<Animal> whiteChunkyFishField = new AnnotatedFieldImpl<Animal>(FishFarm.class.getDeclaredField("realChunkyWhiteFish"), fishFarmClass);
      
      Bean<Cod> codBean = createSimpleBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleBean(Sole.class, manager);
      
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      Set<Bean<Animal>> possibleTargets = manager.getResolutionManager().get(whiteChunkyFishField); 
      assert possibleTargets.size() == 1;
      assert possibleTargets.contains(codBean);
   }
   
   @Test
   public void testMultipleApiTypeWithCurrent() throws Exception
   {
      AnnotatedField<Animal> animalField = new AnnotatedFieldImpl<Animal>(FishFarm.class.getDeclaredField("animal"), fishFarmClass);
      Bean<SeaBass> seaBassBean = createSimpleBean(SeaBass.class, manager);
      Bean<Haddock> haddockBean = createSimpleBean(Haddock.class, manager);
      manager.addBean(seaBassBean);
      manager.addBean(haddockBean);
      Set<Bean<Animal>> possibleTargets = manager.getResolutionManager().get(animalField);
      assert possibleTargets.size() == 2;
      assert possibleTargets.contains(seaBassBean);
      assert possibleTargets.contains(haddockBean);
   }
   
   @Test(groups="resolution") @SpecAssertion(section={"4.9.2", "4.9.4"})
   public void testResolveByType() throws Exception
   {
      Bean<Tuna> tunaBean = createSimpleBean(Tuna.class, manager);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class, manager);
      Bean<SeaBass> seaBassBean = createSimpleBean(SeaBass.class, manager);
      Bean<Haddock> haddockBean = createSimpleBean(Haddock.class, manager);
      
      manager.addBean(tunaBean);
      manager.addBean(salmonBean);
      manager.addBean(haddockBean);
      manager.addBean(seaBassBean);
      
      assert manager.resolveByType(Tuna.class, new CurrentAnnotationLiteral()).size() == 1;
      assert manager.resolveByType(Tuna.class, new CurrentAnnotationLiteral()).contains(tunaBean);
      
      assert manager.resolveByType(Tuna.class).size() == 1;
      assert manager.resolveByType(Tuna.class).contains(tunaBean);
      
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).size() == 3;
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).contains(salmonBean);
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).contains(seaBassBean);
      assert manager.resolveByType(Animal.class, new CurrentAnnotationLiteral()).contains(haddockBean);
   }
   
   @Test(groups="injection") @SpecAssertion(section="2.3.5") 
   public void testAllBindingTypesSpecifiedForResolutionMustAppearOnWebBean()
   {
      Bean<Cod> codBean = createSimpleBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleBean(Sole.class, manager);
      
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
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
   
   @Test(groups="resolution") @SpecAssertion(section="4.9.2")
   public void testResolveByTypeWithTypeParameter() throws Exception
   {
      AnnotatedField<Farmer<ScottishFish>> scottishFishFarmerField = new AnnotatedFieldImpl<Farmer<ScottishFish>>(FishFarm.class.getDeclaredField("scottishFishFarmer"), fishFarmClass);
      
      Bean<ScottishFishFarmer> scottishFishFarmerBean = createSimpleBean(ScottishFishFarmer.class, manager);
      Bean<AnimalFarmer> farmerBean = createSimpleBean(AnimalFarmer.class, manager);
      
      manager.addBean(scottishFishFarmerBean);
      manager.addBean(farmerBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(scottishFishFarmerField);
      resolutionManager.resolveInjectionPoints();
      
      assert manager.resolveByType(new TypeLiteral<Farmer<ScottishFish>>(){}).size() == 1;
      assert manager.resolveByType(new TypeLiteral<Farmer<ScottishFish>>(){}).contains(scottishFishFarmerBean);
   }
   
   @Test(groups={"resolution", "producerMethod"}) @SpecAssertion(section="4.9.2")
   public void testResolveByTypeWithArray() throws Exception
   {
      SimpleBean<SpiderProducer> spiderProducerBean = createSimpleBean(SpiderProducer.class, manager);
      manager.addBean(spiderProducerBean);
      Method method = SpiderProducer.class.getMethod("getSpiders");
      Bean<Spider[]> spidersModel = createProducerMethodBean(Spider[].class, method, manager, spiderProducerBean);
      manager.addBean(spidersModel);
      method = SpiderProducer.class.getMethod("getStrings");
      Bean<String[]> stringModel = createProducerMethodBean(String[].class, method, manager, spiderProducerBean);
      manager.addBean(stringModel);
      
      assert manager.resolveByType(Spider[].class).size() == 1;
   }
   
   @Test @SpecAssertion(section="4.9.2")
   public void testOnlyHighestEnabledPreecedenceWebBeansResolved() throws Exception
   {
      AnnotatedField<Animal> whiteFishField = new AnnotatedFieldImpl<Animal>(FishFarm.class.getDeclaredField("whiteFish"), fishFarmClass);
      Bean<Cod> codBean = createSimpleBean(Cod.class, manager);
      Bean<Sole> soleBean = createSimpleBean(Sole.class, manager);
      Bean<Plaice> plaiceBean = createSimpleBean(Plaice.class, manager);
      
      
      manager.addBean(plaiceBean);
      manager.addBean(codBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteFishField);
      resolutionManager.resolveInjectionPoints();

      assert manager.resolveByType(Animal.class, new AnnotationLiteral<Whitefish>() {}).size() == 1;
      assert manager.resolveByType(Animal.class, new AnnotationLiteral<Whitefish>() {}).contains(plaiceBean);
      
   }
   
   @Test(groups="resolution") @SpecAssertion(section="4.9.2")
   public void testResolveByTypeWithNonBindingMembers() throws Exception
   {
      AnnotatedField<Animal> veryExpensiveWhitefishField = new AnnotatedFieldImpl<Animal>(FishFarm.class.getDeclaredField("veryExpensiveWhitefish"), fishFarmClass);
      Bean<Halibut> halibutBean = createSimpleBean(Halibut.class, manager);
      Bean<RoundWhitefish> roundWhiteFishBean = createSimpleBean(RoundWhitefish.class, manager);
      Bean<Sole> soleBean = createSimpleBean(Sole.class, manager);
      manager.addBean(halibutBean);
      manager.addBean(roundWhiteFishBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(veryExpensiveWhitefishField);
      resolutionManager.resolveInjectionPoints();
      
      Set<Bean<Animal>> beans = manager.resolveByType(Animal.class, new ExpensiveAnnotationLiteral() 
      {

         public int cost()
         {
            return 60;
         }

         public boolean veryExpensive()
         {
            return true;
         }
         
      }, new AnnotationLiteral<Whitefish>() {});
      assert beans.size() == 2;
      assert beans.contains(halibutBean);
      assert beans.contains(roundWhiteFishBean);
   }
   
   @Test(groups="resolution") @SpecAssertion(section="4.9.2")
   public void testNoWebBeansFound() throws Exception
   {
      AnnotatedField<ScottishFish> whiteScottishFishField = new AnnotatedFieldImpl<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"), fishFarmClass);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleBean(Sole.class, manager);
      Bean<Plaice> plaiceBean = createSimpleBean(Plaice.class, manager);
      manager.addBean(plaiceBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      resolutionManager.resolveInjectionPoints();
      
      assert manager.resolveByType(Tuna.class, new CurrentAnnotationLiteral()).size() == 0;
   }
   
   @Test(groups="resolution") @SpecAssertion(section="4.9.2")
   public void testResolveObject() throws Exception
   {
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleBean(Sole.class, manager);
      Bean<Plaice> plaiceBean = createSimpleBean(Plaice.class, manager);
      manager.addBean(plaiceBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      assert manager.resolveByType(Object.class).size() == 4;
      assert manager.resolveByType(Object.class).contains(plaiceBean);
      assert manager.resolveByType(Object.class).contains(salmonBean);
      assert manager.resolveByType(Object.class).contains(soleBean);
      
   }
   
   @Test(groups="resolution", expectedExceptions=DefinitionException.class) @SpecAssertion(section="4.9.2.1")
   public void testArrayValuedAnnotationMemberWithoutNonBinding()
   {
      manager.resolveByType(Animal.class, new BindingTypeWithBindingArrayTypeMemberAnnotationLiteral() {
         
         public boolean[] bool()
         {
            return new boolean[0];
         }
         
      });
   }
   
   @Test(groups="resolution", expectedExceptions=DefinitionException.class) @SpecAssertion(section="4.9.2.1")
   public void testAnnotationValuedAnnotationMemberWithoutNonBinding()
   {
      manager.resolveByType(Animal.class, new BindingTypeWithBindingAnnotationMemberAnnotationLiteral()
      {
         
         public Expensive expensive()
         {
            return new ExpensiveAnnotationLiteral()
            {
               public int cost()
               {
                  return 0;
               }
               
               public boolean veryExpensive()
               {
                  return false;
               }
            };
         }
      
      });
   }
      
}
