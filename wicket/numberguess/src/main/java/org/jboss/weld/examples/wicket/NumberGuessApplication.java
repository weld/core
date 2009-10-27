package org.jboss.weld.examples.wicket;

import org.jboss.weld.wicket.WeldApplication;

public class NumberGuessApplication extends WeldApplication
{
   @Override
   public Class getHomePage()
   {
      return HomePage.class;
   }
}
