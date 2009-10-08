package org.jboss.weld.test.unit.context;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
public class ContextTest extends AbstractWeldTest
{
   // WBRI-155
   @Test(description="WBRI155", groups="stub")
   public void testSessionContextActiveForMultipleSimultaneousThreads()
   {
      // TODO impl
      assert false;
   }
   
}
