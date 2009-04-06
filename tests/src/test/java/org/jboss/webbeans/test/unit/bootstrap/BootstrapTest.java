package org.jboss.webbeans.test.unit.bootstrap;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class BootstrapTest extends AbstractWebBeansTest
{
   
   @Test(groups="bootstrap")
   public void testInitializedEvent()
   {
      assert InitializedObserver.observered;
   }
   
   @Test(groups="bootstrap")
   public void testRequestContextActiveDuringInitializtionEvent()
   {
      assert InitializedObserverWhichUsesRequestContext.name == new Tuna().getName();
   }
   
   @Test(groups={"bootstrap"})
   public void testApplicationContextActiveDuringInitializtionEvent()
   {
      assert Cow.mooed;
   }
   
}
