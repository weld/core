package org.jboss.weld.tests.enterprise;

public class Fumes
{
   
   private int volume;
   
   Fumes(int volume)
   {
      this.volume = volume;
   }

   public void setVolume(int volume)
   {
      this.volume = volume;
   }
   
   public int getVolume()
   {
      return volume;
   }

}
