package org.jboss.weld.environment.tomcat;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.manager.api.WeldManager;

/**
 * A Tomcat lifecycle listener that makes CDI injection possible into Listener
 * components.
 * 
 * <p>
 * Weld Servlet is normally started by the Weld Listener when it receives the
 * context initialized event fired by the Servlet container. However, by that
 * point, Tomcat has already performed resource injections into listeners. In
 * order to get the WeldAnnotationProcessor registered early enough to be able
 * to inject into Listener components, we have to notify the Weld Listener in a
 * Tomcat lifecycle listener.
 * </p>
 * 
 * <p>
 * Since the actual instance of the Weld Listener hasn't been created at this
 * point, we have to swap the instances around before shutdown so that the
 * instance that performed the initialization can also cleanup.
 * </p>
 * 
 * <p>
 * This class must be activated by registering it in the META-INF/context.xml
 * descriptor relative to the web application root:
 * </p>
 * 
 * <pre>
 * &lt;Context&gt;
 *    &lt;Listener className="org.jboss.weld.environment.tomcat.WeldLifecycleListener"/&gt;
 * &lt;/Context&gt;
 * </pre>
 * 
 * @author Dan Allen <dan.j.allen@gmail.com>
 */
public class WeldLifecycleListener implements LifecycleListener
{
   private Listener listener;
   
   public void lifecycleEvent(LifecycleEvent event)
   {
      if (event.getLifecycle() instanceof StandardContext)
      {
         String type = event.getType();
         if (type.equals(StandardContext.AFTER_START_EVENT))
         {
            StandardContext stdCtx = (StandardContext) event.getLifecycle();
            final ServletContext serCtx = stdCtx.getServletContext();
            Listener l = new Listener();
            ServletContextEvent sce = new ServletContextEvent(serCtx);
            // hmmm...can we communicate with the listener more cleanly?
            serCtx.setAttribute(Listener.LISTENER_INJECTION_SUPPORT_ATTRIBUTE_NAME, true);
            l.contextInitialized(sce);
            WeldManager manager = (WeldManager) serCtx.getAttribute(Listener.BEAN_MANAGER_ATTRIBUTE_NAME);
            if (manager != null)
            {
               listener = l;
            }
         }
         else if (type.equals(StandardContext.BEFORE_STOP_EVENT) && listener != null)
         {
            StandardContext stdCtx = (StandardContext) event.getLifecycle();
            Object[] listeners = stdCtx.getApplicationLifecycleListeners();
            List<Object> replacements = new ArrayList<Object>(listeners.length);
            for (Object l : listeners)
            {
               if (l instanceof Listener)
               {
                  replacements.add(listener);
               }
               else
               {
                  replacements.add(l);
               }
            }
            stdCtx.setApplicationLifecycleListeners(replacements.toArray(new Object[replacements.size()]));
         }
      }
   }
}
