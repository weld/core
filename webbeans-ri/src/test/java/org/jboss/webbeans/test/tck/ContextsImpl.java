package org.jboss.webbeans.test.tck;

import org.jboss.webbeans.context.AbstractContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.tck.api.Contexts;

public class ContextsImpl implements Contexts<AbstractContext>
{

   public RequestContext getRequestContext()
   {
      return RequestContext.INSTANCE;
   }

   public void setActive(AbstractContext context)
   {
      context.setActive(true);
   }

   public void setInactive(AbstractContext context)
   {
      context.setActive(false);
   }

   public AbstractContext getDependentContext()
   {
      return DependentContext.INSTANCE;
   }
   
   
   
}
