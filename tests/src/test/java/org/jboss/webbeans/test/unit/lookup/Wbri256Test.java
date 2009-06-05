package org.jboss.webbeans.test.unit.lookup;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class Wbri256Test extends AbstractWebBeansTest
{

   @Test
   public void testParameterizedInjection()
   {
      LookupFoo lookupFoo = getCurrentManager().getInstanceByType(LookupFoo.class);
      assert lookupFoo.getFoo().getName().equals("foo");
      assert lookupFoo.getFoobaz().getName().equals("foobase");
   }
   
}
