package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.bootstrap.api.helpers.AbstractBootstrap;
import org.jboss.webbeans.manager.api.WebBeansManager;

public class MockBootstrap extends AbstractBootstrap
{
   
   public void boot()
   {
      // TODO Auto-generated method stub
      
   }
   
   public WebBeansManager getManager()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public void initialize()
   {
      verify();
   }
   
   public void shutdown()
   {
      // TODO Auto-generated method stub
      
   }
   
}
