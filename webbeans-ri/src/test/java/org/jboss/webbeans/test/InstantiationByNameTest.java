package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedFieldImpl;
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

@SpecVersion("20081206")
public class InstantiationByNameTest extends AbstractTest
{
   
   private AnnotatedClass<FishFarm> fishFarmClass = new AnnotatedClassImpl<FishFarm>(FishFarm.class);
   
   @Test(groups="resolution") @SpecAssertion(section="4.10")
   public void testNoWebBeansFound() throws Exception
   {
      AnnotatedField<ScottishFish> whiteScottishFishField = new AnnotatedFieldImpl<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"), fishFarmClass);
      Bean<Cod> codBean = createSimpleBean(Cod.class);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      
      assert manager.getInstanceByName("foo") == null;
   }
   
   @Test(expectedExceptions=AmbiguousDependencyException.class) @SpecAssertion(section="4.10.1")
   public void testAmbiguousDependencies() throws Exception
   {
      AnnotatedField<ScottishFish> whiteScottishFishField = new AnnotatedFieldImpl<ScottishFish>(FishFarm.class.getDeclaredField("whiteScottishFish"), fishFarmClass);
      Bean<Cod> codBean = createSimpleBean(Cod.class);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class);
      Bean<Sole> soleBean = createSimpleBean(Sole.class);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      
      manager.getInstanceByName("whitefish");
   }
   
   @Test(groups={"resolution", "beanLifecycle"}) @SpecAssertion(section="4.10.1")
   public void testGetInstanceByName()
   {
      Bean<Tuna> tunaBean = createSimpleBean(Tuna.class);
      Bean<Cod> codBean = createSimpleBean(Cod.class);
      Bean<Salmon> salmonBean = createSimpleBean(Salmon.class);
      Bean<Sole> soleBean = createSimpleBean(Sole.class);
      Bean<SeaBass> seaBassBean = createSimpleBean(SeaBass.class);
      Bean<Haddock> haddockBean = createSimpleBean(Haddock.class);
      Bean<Plaice> plaiceBean = createSimpleBean(Plaice.class);
      
      manager.addBean(tunaBean);
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      manager.addBean(soleBean);
      manager.addBean(haddockBean);
      manager.addBean(seaBassBean);
      manager.addBean(plaiceBean);
      
      assert manager.getInstanceByName("salmon") instanceof Salmon;
   }
   
   @Test(groups={"stub", "resolution", "el"}) @SpecAssertion(section="4.10")
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
