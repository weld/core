package org.jboss.webbeans.contexts;

import javax.webbeans.RequestScoped;

public class RequestContext extends PrivateContext
{

   public RequestContext()
   {
      super(RequestScoped.class);
   }

   @Override
   public String toString()
   {
      return "Request context";
   }
}
