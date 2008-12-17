package org.jboss.webbeans.test.beans;

import javax.webbeans.Current;
import javax.webbeans.Named;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.beans.StarFinch.Mess;

@RequestScoped
@Named("BirdCage")
public class BirdCage
{
   @Current
   private Mess someMess;

   public BirdCage()
   {
   }
   
   public Mess getSomeMess()
   {
      return someMess;
   }
}
