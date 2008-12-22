package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Event;
import javax.webbeans.Fires;

public class BlackRumpedWaxbill
{
   @Fires
   private Event simpleEvent;

   public void eliminateWarning()
   {
      assert simpleEvent != null;
   }
}
