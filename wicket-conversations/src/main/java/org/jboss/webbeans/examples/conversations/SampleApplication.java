package org.jboss.webbeans.examples.conversations;

import org.jboss.webbeans.wicket.WebBeansApplication;

public class SampleApplication extends WebBeansApplication
{

   @Override
   public Class getHomePage()
   {
      return HomePage.class;
   }

}
