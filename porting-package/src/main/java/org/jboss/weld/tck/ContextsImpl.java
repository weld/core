package org.jboss.weld.tck;

import org.jboss.jsr299.tck.spi.Contexts;
import org.jboss.weld.Container;
import org.jboss.weld.context.AbstractContext;
import org.jboss.weld.context.AbstractMapContext;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.RequestContext;

public class ContextsImpl implements Contexts<AbstractContext>
{

   public RequestContext getRequestContext()
   {
      return Container.instance().services().get(ContextLifecycle.class).getRequestContext();
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
      return Container.instance().services().get(ContextLifecycle.class).getDependentContext();
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
