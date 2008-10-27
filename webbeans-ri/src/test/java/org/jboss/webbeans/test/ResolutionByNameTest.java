package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.beans.Cod;
import org.jboss.webbeans.test.beans.Haddock;
import org.jboss.webbeans.test.beans.Plaice;
import org.jboss.webbeans.test.beans.Salmon;
import org.jboss.webbeans.test.beans.SeaBass;
import org.jboss.webbeans.test.beans.Sole;
import org.jboss.webbeans.test.beans.Tuna;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ResolutionByNameTest extends AbstractTest
{

   @Test(groups="el") @SpecAssertion(section="4.10")
   public void testELResolver()
   {
      assert false;
   }
   
   @Test(groups="el") @SpecAssertion(section="4.10.2")
   public void testELResolverRegisteredWithJsf()
   {
      assert false;
   }
   
   @Test(groups="el") @SpecAssertion(section="4.10.2")
   public void testELResolverRegisteredWithServlet()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="4.10.1")
   public void testNamedBasedResolution()
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
      
      assert manager.resolveByName("salmon").size() == 1;
      assert manager.resolveByName("salmon").contains(salmonBean);
      
      assert manager.resolveByName("whitefish").size() == 2;
      assert manager.resolveByName("whitefish").contains(codBean);
      assert manager.resolveByName("whitefish").contains(soleBean);
      
      manager.addBean(plaiceBean);
      
      assert manager.resolveByName("whitefish").size() == 1;
      assert manager.resolveByName("whitefish").contains(plaiceBean);
   }
   
   @Test(groups="resolution") @SpecAssertion(section="4.10.1")
   public void testNoWebBeansFound()
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
