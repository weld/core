package org.jboss.webbeans.test.unit.context;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
public class ContextTest extends AbstractWebBeansTest
{
   // WBRI-155
   @Test(groups="stub")
   public void testSessionContextActiveForMultipleSimultaneousThreads()
   {
      assert false;
      // TODO Implement this test
   }
   
}
