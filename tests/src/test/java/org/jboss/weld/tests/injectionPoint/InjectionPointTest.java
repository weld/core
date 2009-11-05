package org.jboss.weld.tests.injectionPoint;

import javax.enterprise.inject.IllegalProductException;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class InjectionPointTest extends AbstractWeldTest
{
   
   @Test(description="WELD-239")
   public void testCorrectInjectionPointUsed()
   {
      getCurrentManager().getInstanceByType(IntConsumer.class).ping();
      
      try
      {
         getCurrentManager().getInstanceByType(DoubleConsumer.class).ping();
      }
      catch (IllegalProductException e)
      {
         assert e.getMessage().contains("Injection Point: field org.jboss.weld.tests.injectionPoint.DoubleGenerator.timer");
      }
   }

}
