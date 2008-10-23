package org.jboss.webbeans.test.components;

import javax.webbeans.Current;
import javax.webbeans.Production;

@Production
public class FishFarm
{

   @Current
   private Tuna tuna;
   
}
