package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Event;
import javax.webbeans.Observable;

public class GoldbreastWaxbill
{
   @Observable
   private Event<?> simpleEvent;

   public void eliminateWarning()
   {
      assert simpleEvent != null;
   }
}
