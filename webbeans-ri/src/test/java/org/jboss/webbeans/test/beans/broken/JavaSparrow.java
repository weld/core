package org.jboss.webbeans.test.beans.broken;

import java.util.ArrayList;

import javax.webbeans.Event;
import javax.webbeans.Fires;

public class JavaSparrow
{
   @Fires
   private Event<ArrayList<String>> simpleEvent;

   public void eliminateWarning()
   {
      assert simpleEvent != null;
   }
}
