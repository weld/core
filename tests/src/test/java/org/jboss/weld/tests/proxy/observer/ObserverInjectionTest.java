package org.jboss.weld.tests.proxy.observer;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ObserverInjectionTest extends AbstractWeldTest
{

   @Test(description="WELD-535", groups="broken")
   public void testInjectionHappens()
   {
      SampleObserver sampleObserver = getReference(SampleObserver.class);
      assert !sampleObserver.isInjectionAndObservationOccured();
      getCurrentManager().fireEvent(new Baz());
      assert sampleObserver.isInjectionAndObservationOccured();
   }
   
}
