package org.jboss.webbeans.test;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.components.Cod;
import org.jboss.webbeans.test.components.Haddock;
import org.jboss.webbeans.test.components.Plaice;
import org.jboss.webbeans.test.components.Salmon;
import org.jboss.webbeans.test.components.SeaBass;
import org.jboss.webbeans.test.components.Sole;
import org.jboss.webbeans.test.components.Tuna;
import org.testng.annotations.Test;

public class NameBasedResolutionTest extends AbstractTest
{

   @Test
   public void testNamedBasedResolution()
   {
      Bean<Tuna> tunaBean = new BeanImpl<Tuna>(new SimpleComponentModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), getEmptyAnnotatedItem(Tuna.class), super.manager), manager);
      Bean<Cod> codBean = new BeanImpl<Cod>(new SimpleComponentModel<Cod>(new SimpleAnnotatedType<Cod>(Cod.class), getEmptyAnnotatedItem(Cod.class), super.manager), manager);
      Bean<Salmon> salmonBean = new BeanImpl<Salmon>(new SimpleComponentModel<Salmon>(new SimpleAnnotatedType<Salmon>(Salmon.class), getEmptyAnnotatedItem(Salmon.class), super.manager), manager);
      Bean<Sole> soleBean = new BeanImpl<Sole>(new SimpleComponentModel<Sole>(new SimpleAnnotatedType<Sole>(Sole.class), getEmptyAnnotatedItem(Sole.class), super.manager), manager);
      Bean<SeaBass> seaBassBean = new BeanImpl<SeaBass>(new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedItem(SeaBass.class), super.manager), manager);
      Bean<Haddock> haddockBean = new BeanImpl<Haddock>(new SimpleComponentModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), getEmptyAnnotatedItem(Haddock.class), super.manager), manager);
      Bean<Plaice> plaiceBean = new BeanImpl<Plaice>(new SimpleComponentModel<Plaice>(new SimpleAnnotatedType<Plaice>(Plaice.class), getEmptyAnnotatedItem(Plaice.class), super.manager), manager);
      
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
   
}
