package org.jboss.webbeans.test.beans.broken;

import java.util.ArrayList;

import javax.webbeans.Observable;

public class CommonWaxbill
{
   @Observable
   private ArrayList<String> simpleEvent;

   public void eliminateWarning()
   {
      assert simpleEvent != null;
   }
}
