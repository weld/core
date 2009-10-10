package org.jboss.weld.examples.wicket;

import org.jboss.weld.wicket.WeldApplication;

public class SampleApplication extends WeldApplication
{

   @Override
   public Class getHomePage()
   {
      return HomePage.class;
   }

}
