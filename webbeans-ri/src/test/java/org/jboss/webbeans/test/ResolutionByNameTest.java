package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;

import javax.webbeans.manager.Bean;

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
      
      Bean<Tuna> tunaBean = createSimpleWebBean(Tuna.class, manager);
      Bean<Cod> codBean = createSimpleWebBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleWebBean(Salmon.class, manager);
      Bean<Sole> soleBean = createSimpleWebBean(Sole.class, manager);
      Bean<SeaBass> seaBassBean = createSimpleWebBean(SeaBass.class, manager);
      Bean<Haddock> haddockBean = createSimpleWebBean(Haddock.class, manager);
      Bean<Plaice> plaiceBean = createSimpleWebBean(Plaice.class, manager);
      
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
   public void testNoWebBeansFound() throws Exception
   {
      Bean<Cod> codBean = createSimpleWebBean(Cod.class, manager);
      Bean<Salmon> salmonBean = createSimpleWebBean(Salmon.class, manager);
      
      manager.addBean(codBean);
      manager.addBean(salmonBean);
      
      assert manager.resolveByName("foo").size() == 0;
   }
   
   /*

   @Test(groups="el") @SpecAssertion(section="4.10")
   public void test
   {
      assert false;
   }

    */
   
}
