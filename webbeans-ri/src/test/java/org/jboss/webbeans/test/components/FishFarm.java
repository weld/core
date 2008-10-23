package org.jboss.webbeans.test.components;

import javax.webbeans.Current;
import javax.webbeans.Production;

@Production
public class FishFarm
{

   @SuppressWarnings("unused")
   @Current
   private Tuna tuna;
   
   @SuppressWarnings("unused")
   @Current
   private Animal animal;
   
}
