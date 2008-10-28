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
