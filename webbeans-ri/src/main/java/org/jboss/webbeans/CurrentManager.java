package org.jboss.webbeans;

import org.jboss.webbeans.contexts.ApplicationContext;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.contexts.SessionContext;

public class CurrentManager {

   protected static ManagerImpl rootManager;

   public static ManagerImpl rootManager()
   {
      return rootManager;
   }
   
   public static void setRootManager(ManagerImpl rootManager) {
      CurrentManager.rootManager = rootManager;
   }

   /**
    * Set up the root manager. 
    * TODO: move this to Bootstrap
    */
   static {
      rootManager = new ManagerImpl();
      rootManager.addContext(RequestContext.INSTANCE);
      rootManager.addContext(SessionContext.INSTANCE);
      rootManager.addContext(ApplicationContext.INSTANCE);
   }
   
}
