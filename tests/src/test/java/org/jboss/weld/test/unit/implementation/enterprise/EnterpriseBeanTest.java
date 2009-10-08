package org.jboss.weld.test.unit.implementation.enterprise;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@Packaging(PackagingType.EAR)
public class EnterpriseBeanTest extends AbstractWeldTest
{
   
   @Test(description="WBRI-179")
   public void testSFSBWithOnlyRemoteInterfacesDeploys()
   {
      
   }
   
   @Test(description="WBRI-275")
   public void testSLSBBusinessMethodThrowsRuntimeException()
   {
      try
      {
         getCurrentManager().getInstanceByType(Fedora.class).causeRuntimeException();
      }
      catch (Throwable t) 
      {
         if (isExceptionInHierarchy(t, BowlerHatException.class))
         {
            return;
         }
      }
      assert false : "Expected a BowlerHatException to be in the cause stack";
   }
   
   
}
