package org.jboss.arquillian.container.weld.ee.embedded_1_1;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.Event;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.manager.api.WeldManager;

/**
 * DestorySession
 *
 * @author <a href="mailto:aknutsen@redhat.org">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class SessionLifeCycleDestroyer implements EventHandler<Event> {
   
   public void callback(Context context, Event event) throws Exception
   {
      WeldManager manager = context.get(WeldManager.class);
      
      BoundSessionContext sessionContext = manager.instance().select(BoundSessionContext.class).get();
      CDISessionMap map = context.get(CDISessionMap.class);
      if(map != null)
      {
         try
         {
            sessionContext.invalidate();
            sessionContext.deactivate();
         }
         finally
         {
            sessionContext.dissociate(map);
         }
      }
   }
}
