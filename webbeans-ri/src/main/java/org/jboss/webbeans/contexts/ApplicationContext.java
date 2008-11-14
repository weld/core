package org.jboss.webbeans.contexts;

import java.util.Map;

import javax.webbeans.ApplicationScoped;

public class ApplicationContext extends NormalContext {

   public ApplicationContext()
   {
      super(ApplicationScoped.class);
   }
   
   public ApplicationContext(Map<String, Object> data)
   {
      super(ApplicationScoped.class, data);
   }   
   
   @Override
   public String toString()
   {
      return "Application context";
   }   

}
