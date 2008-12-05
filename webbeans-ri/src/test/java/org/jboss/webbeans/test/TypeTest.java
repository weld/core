package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.beans.Haddock;
import org.testng.annotations.Test;

public class TypeTest extends AbstractTest
{

   @Test @SpecAssertion(section="2.6.3")
   public void testDefaultNamed()
   {
      SimpleBean<Haddock> haddock = createSimpleBean(Haddock.class);
      assert haddock.getName() != null;
      assert haddock.getName().equals("haddock");
   }
   
}

