package org.jboss.arquillian.container.weld.ee.embedded_1_1;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.Event;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.manager.api.WeldManager;

/**
 * DestorySession
 *
 * @author <a href="mailto:aknutsen@redhat.org">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class SessionLifeCycleDestoryer implements EventHandler<Event> {
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.event.EventHandler#callback(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, Event event) throws Exception
   {
      WeldManager manager = context.get(WeldManager.class);
      CDISessionID id = context.get(CDISessionID.class);
      if(id != null)
      {
         manager.getServices().get(ContextLifecycle.class).endSession(id.getId(), id.getBeanStore());
      }
   }
}
