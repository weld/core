package org.jboss.webbeans.test.components;

import javax.webbeans.Current;
import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Chunky;
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
   private ScottishFish scottishFish;
   
   @Whitefish @Chunky
   private Animal whiteChunkyFish;
   
   private Farmer<ScottishFish> scottishFishFarmer;
   
}
