package org.jboss.webbeans.test.unit.lookup;

import javax.inject.Current;
import javax.inject.Production;

@Production
class FishFarm
{

   @SuppressWarnings("unused")
   @Current
   public Tuna tuna;
   
   @SuppressWarnings("unused")
   @Current
   public Animal animal;
   
   @SuppressWarnings("unused")
   @Whitefish
   public ScottishFish whiteScottishFish;
   
   @SuppressWarnings("unused")
   @Whitefish
   public Animal whiteFish;
   
   @SuppressWarnings("unused")
   @Whitefish @Chunky(realChunky=true)
   public Animal realChunkyWhiteFish;
   
   @SuppressWarnings("unused")
   @Current
   public Farmer<ScottishFish> scottishFishFarmer;
   
   @Expensive(cost=60, veryExpensive=true) @Whitefish
   public Animal veryExpensiveWhitefish;
   
}
