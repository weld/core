package org.jboss.webbeans.test.beans;

import javax.webbeans.Named;
import javax.webbeans.Observes;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.beans.StarFinch.Mess;

@RequestScoped
@Named("FinchKeeper")
public class FinchKeeper
{
   public FinchKeeper()
   {
   }

   private boolean newMessDetected = false;

   public void observesMesses(@Observes Mess aNewMess)
   {
      newMessDetected = true;
   }
   
   public boolean isNewMessDetected()
   {
      return newMessDetected;
   }
}
