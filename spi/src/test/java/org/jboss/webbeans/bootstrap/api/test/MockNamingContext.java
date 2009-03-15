package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.resources.spi.NamingContext;

public class MockNamingContext implements NamingContext
{
   
   public void bind(String name, Object value)
   {
      // TODO Auto-generated method stub
      
   }
   
   public <T> T lookup(String name, Class<? extends T> expectedType)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
}
