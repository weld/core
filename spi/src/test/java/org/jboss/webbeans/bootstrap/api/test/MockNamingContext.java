package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.resources.spi.NamingContext;

public class MockNamingContext implements NamingContext
{
   
   public void bind(String name, Object value)
   {
   }
   
   public <T> T lookup(String name, Class<? extends T> expectedType)
   {
      return null;
   }

   public void unbind(String key)
   {
      
   }
   
}
