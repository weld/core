package org.jboss.webbeans.tck;

import org.jboss.jsr299.tck.spi.Contexts;
import org.jboss.webbeans.context.AbstractBeanMapContext;
import org.jboss.webbeans.context.AbstractContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;

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
   
   public void destroyContext(AbstractContext context)
   {
      if (context instanceof AbstractBeanMapContext)
      {
         ((AbstractBeanMapContext) context).destroy();
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }
   
}
