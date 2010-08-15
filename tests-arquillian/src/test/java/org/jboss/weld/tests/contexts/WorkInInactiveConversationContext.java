package org.jboss.weld.tests.contexts;

import org.jboss.weld.Container;
import org.jboss.weld.context.ContextLifecycle;


public abstract class WorkInInactiveConversationContext extends WorkInInactiveContext
{

   @Override
   protected void activateContext()
   {
      Container.instance().services().get(ContextLifecycle.class).getRequestContext().setActive(true);
   }
   
   @Override
   protected void deactivateContext()
   {
      Container.instance().services().get(ContextLifecycle.class).getRequestContext().setActive(false);
   }
   
   @Override
   protected boolean isContextActive()
   {
      return Container.instance().services().get(ContextLifecycle.class).isRequestActive();
   }

}
