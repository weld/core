package org.jboss.webbeans.contexts;

import javax.webbeans.RequestScoped;

/**
 * The request context
 * 
 * @author Nicklas Karlsson
 */
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
