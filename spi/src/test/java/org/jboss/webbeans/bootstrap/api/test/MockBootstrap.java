package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.helpers.AbstractBootstrap;
import org.jboss.webbeans.manager.api.WebBeansManager;

public class MockBootstrap extends AbstractBootstrap
{
   
   public WebBeansManager getManager()
   {
      return null;
   }
   
   public Bootstrap startContainer()
   {
      verify();
      return this;
   }
   
   
   
   public void shutdown()
   {
   }

   public Bootstrap deployBeans()
   {
      return this;
   }

   public Bootstrap endInitialization()
   {
      return this;
   }

   public Bootstrap startInitialization()
   {
      return this;
   }

   public Bootstrap validateBeans()
   {
      return this;
   }
   
}
