package org.jboss.webbeans.test.beans;

import javax.webbeans.IfExists;
import javax.webbeans.Observes;

/**
 * Simple web bean that conditionally listens to events.
 *
 */
public class RecluseSpider
{
   public static boolean notified = false;
   
   public void observe(@Observes @IfExists String someEvent)
   {
      notified = true;
   }
}
