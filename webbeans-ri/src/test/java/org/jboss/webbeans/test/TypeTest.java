package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleModel;

import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.beans.Haddock;
import org.testng.annotations.Test;

public class TypeTest extends AbstractTest
{

   @Test @SpecAssertion(section="2.6.3")
   public void testDefaultNamed()
   {
      SimpleBeanModel<Haddock> haddock = createSimpleModel(Haddock.class, manager);
      assert haddock.getName() != null;
      assert haddock.getName().equals("haddock");
   }
   
}

