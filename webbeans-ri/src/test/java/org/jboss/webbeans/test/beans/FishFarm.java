package org.jboss.webbeans.test.beans;

import javax.webbeans.Current;
import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Chunky;
import org.jboss.webbeans.test.annotations.Expensive;
import org.jboss.webbeans.test.annotations.Whitefish;

@Production
public class FishFarm
{

   @SuppressWarnings("unused")
   @Current
   private Tuna tuna;
   
   @SuppressWarnings("unused")
   @Current
   private Animal animal;
   
   @SuppressWarnings("unused")
   @Whitefish
   private ScottishFish whiteScottishFish;
   
   @SuppressWarnings("unused")
   @Whitefish
   private Animal whiteFish;
   
   @SuppressWarnings("unused")
   @Whitefish @Chunky(realChunky=true)
   private Animal realChunkyWhiteFish;
   
   @SuppressWarnings("unused")
   @Current
   private Farmer<ScottishFish> scottishFishFarmer;
   
   @Expensive(cost=60, veryExpensive=true) @Whitefish
   private Animal veryExpensiveWhitefish;
   
}
