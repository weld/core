package org.jboss.webbeans.examples.wicket;

import org.apache.wicket.protocol.http.HttpSessionStore;
import org.apache.wicket.session.ISessionStore;
import org.jboss.webbeans.wicket.WebBeansApplication;

public class SampleApplication extends WebBeansApplication
{
   
   @Override
   protected void init()
   {
      // GAE doesn't allow resource modification watching
      getResourceSettings().setResourcePollFrequency(null);
      
      //
   }
   
   @Override
   protected ISessionStore newSessionStore() 
   {  
      // GAE doesn't allow disk access (default for Wicket) 
      return new HttpSessionStore(this);  
   }  

   @Override
   public Class<?> getHomePage()
   {
      return HomePage.class;
   }

}
