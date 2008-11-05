package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.beans.Haddock;
import org.testng.annotations.Test;

public class TypeTest extends AbstractTest
{

   @Test @SpecAssertion(section="2.6.3")
   public void testDefaultNamed()
   {
      SimpleBeanModel<Haddock> haddock = new SimpleBeanModel<Haddock>(new SimpleAnnotatedClass<Haddock>(Haddock.class), getEmptyAnnotatedType(Haddock.class), manager);
      assert haddock.getName() != null;
      assert haddock.getName().equals("haddock");
   }
   
}

