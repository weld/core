package org.jboss.webbeans.contexts;

import javax.webbeans.RequestScoped;

public class RequestContext extends NormalContext {

   public RequestContext()
   {
      super(RequestScoped.class);
   }

}
