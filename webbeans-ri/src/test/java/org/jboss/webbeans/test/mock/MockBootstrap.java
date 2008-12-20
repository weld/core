package org.jboss.webbeans.test.mock;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.WebBeansBootstrap;

public class MockBootstrap extends WebBeansBootstrap
{ 
   
   public MockBootstrap(ManagerImpl manager)
   {
      super(manager);
   }
   
   public void registerStandardBeans()
   {
      manager.setBeans(createStandardBeans());
   }
   
   @Override
   protected void registerManager()
   {
      CurrentManager.setRootManager(manager);
   }
   
}
