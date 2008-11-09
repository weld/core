package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleBean;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ResolutionManager;
import org.jboss.webbeans.introspector.impl.InjectableField;
import org.jboss.webbeans.test.beans.Cod;
import org.jboss.webbeans.test.beans.FishFarm;
import org.jboss.webbeans.test.beans.Haddock;
import org.jboss.webbeans.test.beans.Plaice;
import org.jboss.webbeans.test.beans.Salmon;
import org.jboss.webbeans.test.beans.ScottishFish;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.beans.Sole;
import org.jboss.webbeans.test.beans.Tuna;
import org.testng.annotations.Test;

public class InstantiationByNameTest extends AbstractTest
{
   
   @Test(groups="resolution") @SpecAssertion(section="4.10")
   public void testNoWebBeansFound() throws Exception
   {
      InjectableField<ScottishFish> whiteScottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      Bean<Cod> codBean = createSimpleBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class, manager);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      
      assert manager.getInstanceByName("foo") == null;
   }
   
   @Test(expectedExceptions=AmbiguousDependencyException.class) @SpecAssertion(section="4.10.1")
   public void testAmbiguousDependencies() throws Exception
   {
      InjectableField<ScottishFish> whiteScottishFishField = new InjectableField<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"));
      Bean<Cod> codBean = createSimpleBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleBean(Sole.class, manager);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      
      manager.getInstanceByName("whitefish");
   }
   
   @Test(groups={"resolution", "beanLifecycle"}) @SpecAssertion(section="4.10.1")
   public void testGetInstanceByName()
   {
      Bean<Tuna> tunaBean = createSimpleBean(Tuna.class, manager);
      Bean<Cod> codBean = createSimpleBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleBean(Sole.class, manager);
      Bean<SeaBass> seaBassBean = createSimpleBean(SeaBass.class, manager);
      Bean<Haddock> haddockBean = createSimpleBean(Haddock.class, manager);
      Bean<Plaice> plaiceBean = createSimpleBean(Plaice.class, manager);
      
      manager.addBean(tunaBean);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      manager.addBean(haddockBean);
      manager.addBean(seaBassBean);
      manager.addBean(plaiceBean);
      
      assert manager.getInstanceByName("salmon") instanceof Salmon;
   }
   
   @Test(groups={"resolution", "el"}) @SpecAssertion(section="4.10")
   public void testGetInstanceByNameCalledOncePerDistinctNameInElExpression()
   {
      assert false;
   }
   
   /*

   @Test(groups="el") @SpecAssertion(section="4.10")
   public void test
   {
      assert false;
   }

    */
   
}
