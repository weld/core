package org.jboss.weld.tck;

import org.jboss.jsr299.tck.spi.Contexts;
import org.jboss.webbeans.Container;
import org.jboss.webbeans.context.AbstractContext;
import org.jboss.webbeans.context.AbstractMapContext;
import org.jboss.webbeans.context.ContextLifecycle;
import org.jboss.webbeans.context.RequestContext;

public class ContextsImpl implements Contexts<AbstractContext>
{

   public RequestContext getRequestContext()
   {
      return Container.instance().deploymentServices().get(ContextLifecycle.class).getRequestContext();
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
      return Container.instance().deploymentServices().get(ContextLifecycle.class).getDependentContext();
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
