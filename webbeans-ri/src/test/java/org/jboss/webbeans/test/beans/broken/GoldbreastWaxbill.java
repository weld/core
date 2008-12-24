package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Event;
import javax.webbeans.Fires;

public class GoldbreastWaxbill
{
   @Fires
   private Event<?> simpleEvent;

   public void eliminateWarning()
   {
      assert simpleEvent != null;
   }
}
