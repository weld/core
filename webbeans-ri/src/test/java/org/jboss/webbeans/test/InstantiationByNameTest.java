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
import org.jboss.webbeans.test.beans.Salmon;
import org.jboss.webbeans.test.beans.ScottishFish;
import org.jboss.webbeans.test.beans.Sole;
import org.testng.annotations.Test;

public class InstantiationByNameTest extends AbstractTest
{
   
   @Test(groups="resolution") @SpecAssertion(section="4.10")
   public void testNoWebBeansFound()
   {
      assert false;
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
   
   @Test(groups="resolution") @SpecAssertion(section="4.10.1")
   public void testGetInstanceByName()
   {
      assert false;
   }
   
   @Test(groups="resolution") @SpecAssertion(section="4.10")
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
