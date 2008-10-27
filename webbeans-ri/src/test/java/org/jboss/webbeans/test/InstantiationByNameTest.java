package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.ResolutionManager;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
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
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedType(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleBeanModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedType(Salmon.class), super.manager), manager);
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
      Bean<Cod> plaiceBean = new BeanImpl<Cod>(new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedType(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleBeanModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedType(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleBeanModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedType(Sole.class), super.manager), manager);
      manager.addBean(plaiceBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      ResolutionManager resolutionManager = manager.getResolutionManager();
      resolutionManager.addInjectionPoint(whiteScottishFishField);
      
      manager.getInstanceByName("whitefish");
   }
   
   @Test(groups={"resolution", "beanLifecycle"}) @SpecAssertion(section="4.10.1")
   public void testGetInstanceByName()
   {
      Bean<Tuna> tunaBean = new BeanImpl<Tuna>(new SimpleBeanModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), getEmptyAnnotatedType(Tuna.class), super.manager), manager);
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleBeanModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedType(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleBeanModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedType(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleBeanModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedType(Sole.class), super.manager), manager);
      Bean<SeaBass> seaBassBean = new BeanImpl<SeaBass>(new SimpleBeanModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedType(SeaBass.class), super.manager), manager);
      Bean<Haddock> haddockBean = new BeanImpl<Haddock>(new SimpleBeanModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), getEmptyAnnotatedType(Haddock.class), super.manager), manager);
      Bean<Plaice> plaiceBean = new BeanImpl<Plaice>(new SimpleBeanModel<Plaice>(new SimpleAnnotatedType<Plaice>(Plaice.class), getEmptyAnnotatedType(Plaice.class), super.manager), manager);
      
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
