package org.jboss.webbeans.test.mock;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.resources.spi.Naming;

public class MockManagerImpl extends ManagerImpl
{
   
   private Naming naming = new Naming()
   {

      public void bind(String key, Object value)
      {
         // no-op
      }

      public <T> T lookup(String name, Class<? extends T> expectedType)
      {
         // No-op
         return null;
      }
      
   };
   
   public static int BUILT_IN_BEANS = 3;
   
   @Override
   public Naming getNaming()
   {
      return naming;
   }
   
}
