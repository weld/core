package org.jboss.webbeans.test.beans;

import javax.webbeans.IfExists;
import javax.webbeans.Observes;
import javax.webbeans.RequestScoped;

/**
 * Simple web bean that conditionally listens to events.
 *
 */
@RequestScoped
public class RecluseSpider
{
   public static boolean notified = false;
   
   public void observe(@Observes @IfExists String someEvent)
   {
      notified = true;
   }
}
