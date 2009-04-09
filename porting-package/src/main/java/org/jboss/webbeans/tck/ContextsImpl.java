package org.jboss.webbeans.tck;

import org.jboss.jsr299.tck.spi.Contexts;
import org.jboss.webbeans.context.AbstractContext;
import org.jboss.webbeans.context.AbstractMapContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;

public class ContextsImpl implements Contexts<AbstractContext>
{

   public RequestContext getRequestContext()
   {
      return RequestContext.instance();
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
      return DependentContext.instance();
   }
   
   public void destroyContext(AbstractContext context)
   {
      if (context instanceof AbstractMapContext)
      {
         ((AbstractMapContext) context).destroy();
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }
   
}
