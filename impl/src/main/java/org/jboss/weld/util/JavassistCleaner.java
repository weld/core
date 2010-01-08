package org.jboss.weld.util;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.weld.bootstrap.api.Service;

public class JavassistCleaner implements Service
{
   
   private final Collection<CleanableMethodHandler> cleanableMethodHandlers;
   
   public JavassistCleaner()
   {
      this.cleanableMethodHandlers = new ArrayList<CleanableMethodHandler>();
   }
   
   public void add(CleanableMethodHandler cleanableMethodHandler)
   {
      cleanableMethodHandlers.add(cleanableMethodHandler);
   }
   
   public void cleanup()
   {
      for (CleanableMethodHandler cleanableMethodHandler : cleanableMethodHandlers)
      {
         cleanableMethodHandler.clean();
      }
      cleanableMethodHandlers.clear();
   }
   
}