package org.jboss.webbeans.test.beans.broken;

import java.util.ArrayList;

import javax.webbeans.Fires;

public class CommonWaxbill
{
   @Fires
   private ArrayList<String> simpleEvent;

   public void eliminateWarning()
   {
      assert simpleEvent != null;
   }
}
