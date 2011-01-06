package org.jboss.weld.environment.jetty7;


import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.List;

/**
 * Jetty 7 Configuration to setup Weld environment
 * (requires Jetty 7.2 or higher)
 *
 * @author <a href="mailto:ben@bulletproof.com.au">Ben Sommerville</a>
 *
 */
public class WeldConfiguration extends AbstractConfiguration
{

   @Override
   public void preConfigure(WebAppContext context) throws Exception
   {
      context.addDecorator(new WeldDecorator(context.getServletContext()));
   }

   @Override
   public void cloneConfigure(WebAppContext template, WebAppContext context) throws Exception
   {
      context.addDecorator(new WeldDecorator(context.getServletContext()));
   }

   @Override
   public void configure(WebAppContext context) throws Exception
   {
   }

   @Override
   public void postConfigure(WebAppContext context) throws Exception
   {
   }

   @Override
   public void deconfigure(WebAppContext context) throws Exception
   {
      // releases injections on Listener components
      final List<ServletContextHandler.Decorator> decorators = context.getDecorators();
      if(decorators == null)
      {
         return;
      }

      for(ServletContextHandler.Decorator decorator :decorators)
      {
         if( decorator instanceof WeldDecorator )
         {
            ((WeldDecorator) decorator).deconfigure();
         }
      }

   }


   public static void register(WebAppContext wac) throws Exception
   {
      // Check to make sure that Weld has not already been registered for this app
      if( isWeldConfigured(wac)) {
         return;
      }

      final WeldConfiguration configuration = new WeldConfiguration();
      configuration.preConfigure(wac);
      configuration.configure(wac);

      appendConfiguration(wac, configuration);
   }

   private static boolean isWeldConfigured(WebAppContext wac) {
      Configuration[] existing = wac.getConfigurations();
      for( Configuration config : existing)
      {
         if( config instanceof WeldConfiguration ) {
            return true;
         }
      }
      return false;
   }

   private static void appendConfiguration(WebAppContext ctx, Configuration c)
   {
      Configuration[] existing = ctx.getConfigurations();
      Configuration[] updated = new Configuration[existing.length + 1];
      System.arraycopy(existing, 0, updated, 0, existing.length);
      updated[existing.length] = c;
      ctx.setConfigurations(updated);
   }

}

