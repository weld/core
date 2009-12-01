package org.jboss.weld.tests.injectionPoint;

import javax.enterprise.inject.IllegalProductException;
import javax.enterprise.inject.spi.InjectionPoint;

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
   
   @Test(description="WELD-316")
   public void testFieldInjectionPointSerializability() throws Throwable
   {
      getCurrentManager().getInstanceByType(StringConsumer.class).ping();
      InjectionPoint ip = StringGenerator.getInjectionPoint();
      assert ip != null;
      assert ip.getMember().getName().equals("str");
      InjectionPoint ip1 = deserialize(serialize(ip));
      assert ip1.getMember().getName().equals("str");
   }

}
