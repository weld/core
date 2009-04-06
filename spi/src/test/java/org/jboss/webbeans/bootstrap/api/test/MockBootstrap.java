package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.bootstrap.api.helpers.AbstractBootstrap;
import org.jboss.webbeans.manager.api.WebBeansManager;

public class MockBootstrap extends AbstractBootstrap
{
   
   public void boot()
   {
      
   }
   
   public WebBeansManager getManager()
   {
      return null;
   }
   
   public void initialize()
   {
      verify();
   }
   
   public void shutdown()
   {
   }
   
}
