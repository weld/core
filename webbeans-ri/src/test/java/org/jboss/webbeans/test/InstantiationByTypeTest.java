package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.AnnotationLiteral;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ResolutionManager;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.test.annotations.Whitefish;
import org.jboss.webbeans.test.beans.Cod;
import org.jboss.webbeans.test.beans.FishFarm;
import org.jboss.webbeans.test.beans.Plaice;
import org.jboss.webbeans.test.beans.Salmon;
import org.jboss.webbeans.test.beans.ScottishFish;
import org.jboss.webbeans.test.beans.Sole;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.beans.broken.PlaiceFarm;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class InstantiationByTypeTest extends AbstractTest
{
   
   @Test(groups={"resolution", "beanLifecycle"}) @SpecAssertion(section="4.9")
   public void testCurrentBindingTypeAssumed()
   {
      Bean<Tuna> tunaBean = createSimpleWebBean(Tuna.class, manager);
      manager.addBean(tunaBean);
      assert manager.getInstanceByType(Tuna.class) != null;
   }
   
   @Test(groups="resolution", expectedExceptions=DuplicateBindingTypeException.class) @SpecAssertion(section="4.9")
   public void testDuplicateBindingTypesUsed()
   {
      manager.getInstanceByType(Tuna.class, new CurrentAnnotationLiteral(), new CurrentAnnotationLiteral());
   }
   
   @Test(groups="resolution", expectedExceptions=IllegalArgumentException.class) @SpecAssertion(section="4.9")
   public void testNonBindingTypeUsed()
   {
      manager.getInstanceByType(Tuna.class, new AnotherDeploymentTypeAnnotationLiteral());
   }
   
   @Test(expectedExceptions=AmbiguousDependencyException.class) @SpecAssertion(section="4.9")
   public void testAmbiguousDependencies() throws Exception
   {
      InjectableField<ScottishFish> whiteScottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      Bean<Cod> codBean = createSimpleWebBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleWebBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleWebBean(Sole.class, manager);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      resolutionManager.resolveInjectionPoints();
      
      manager.getInstanceByType(ScottishFish.class, new AnnotationLiteral<Whitefish>(){});
   }
   
   @Test(expectedExceptions=UnsatisfiedDependencyException.class) @SpecAssertion(section="4.9")
   public void testUnsatisfiedDependencies() throws Exception
   {
      InjectableField<ScottishFish> whiteScottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      Bean<Cod> codBean = createSimpleWebBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleWebBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleWebBean(Sole.class, manager);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      resolutionManager.resolveInjectionPoints();
      
      manager.getInstanceByType(Tuna.class, new CurrentAnnotationLiteral());
   }
   
   @Test(expectedExceptions=UnproxyableDependencyException.class) @SpecAssertion(section="4.9")
   public void testUnproxyableDependencies() throws Exception
   {
      InjectableField<Plaice> plaiceField = new InjectableField<Plaice>(PlaiceFarm.class.getDeclaredField("plaice"));
      Bean<Plaice> plaiceBean = createSimpleWebBean(Plaice.class, manager);
      manager.addBean(plaiceBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(plaiceField);
      resolutionManager.resolveInjectionPoints();
      
      manager.getInstanceByType(Plaice.class, new AnnotationLiteral<Whitefish>(){});
   }
   
   /*

   @Test(groups="resolution") @SpecAssertion(section="4.9")
   public void test
   {
      assert false;
   }

   */  
   
}
