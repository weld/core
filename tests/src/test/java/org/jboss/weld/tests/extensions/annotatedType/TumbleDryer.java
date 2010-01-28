package org.jboss.weld.tests.extensions.annotatedType;

import javax.inject.Inject;


public class TumbleDryer
{

   
   private Plug plug;
   
   @Inject @Original
   private Coins coins;
   
   private final Clothes clothers;

   private RunningTime runningTime;

   private final SerialNumber serialNumber;

   private HotAir hotAir;
   
   public TumbleDryer(@Original Clothes clothes)
   {
      this.clothers = clothes;
      this.serialNumber = null;
   }
   
   @Inject
   public TumbleDryer(SerialNumber serialNumber)
   {
      this.serialNumber = serialNumber;
      this.clothers = null;
   }
   
   public void setRunningTime(@Original RunningTime runningTime)
   {
      this.runningTime = runningTime;  
   }
   
   @Inject
   public void setHotAir(HotAir hotAir)
   {
      this.hotAir = hotAir;
   }
   
   public Plug getPlug()
   {
      return plug;
   }
   
   public Clothes getClothes()
   {
      return clothers;
   }
   
   public HotAir getHotAir()
   {
      return hotAir;
   }
   
   public RunningTime getRunningTime()
   {
      return runningTime;
   }
   
   public SerialNumber getSerialNumber()
   {
      return serialNumber;
   }
   
   public Coins getCoins()
   {
      return coins;
   }
   
}
