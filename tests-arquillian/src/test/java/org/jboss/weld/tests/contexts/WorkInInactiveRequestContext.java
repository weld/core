package org.jboss.weld.tests.contexts;

import org.jboss.weld.Container;
import org.jboss.weld.context.ContextLifecycle;


public abstract class WorkInInactiveRequestContext extends WorkInInactiveContext
{

   @Override
   protected void activateContext()
   {
      Container.instance().services().get(ContextLifecycle.class).getConversationContext().setActive(true);
   }
   
   @Override
   protected void deactivateContext()
   {
      Container.instance().services().get(ContextLifecycle.class).getConversationContext().setActive(false);
   }
   
   @Override
   protected boolean isContextActive()
   {
      return Container.instance().services().get(ContextLifecycle.class).isConversationActive();
   }

}
