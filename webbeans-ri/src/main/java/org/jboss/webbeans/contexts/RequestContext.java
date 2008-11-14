package org.jboss.webbeans.contexts;

import java.util.Map;

import javax.webbeans.RequestScoped;

public class RequestContext extends NormalContext
{

   public RequestContext()
   {
      super(RequestScoped.class);
   }

   public RequestContext(Map<String, Object> data)
   {
      super(RequestScoped.class, data);
   }

   @Override
   public String toString()
   {
      return "Request context";
   }
}
