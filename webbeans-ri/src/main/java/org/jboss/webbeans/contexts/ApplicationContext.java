package org.jboss.webbeans.contexts;

import javax.webbeans.ApplicationScoped;

public class ApplicationContext extends SharedContext {

   public ApplicationContext()
   {
      super(ApplicationScoped.class);
   }
   
   @Override
   public String toString()
   {
      return "Application context";
   }   

}
