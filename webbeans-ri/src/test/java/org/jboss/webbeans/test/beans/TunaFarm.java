package org.jboss.webbeans.test.beans;

import javax.webbeans.Current;
import javax.webbeans.Production;

@Production
public class TunaFarm
{

   @SuppressWarnings("unused")
   @Current
   public Tuna tuna;
   
   public Tuna notInjectedTuna = new Tuna();

   
}
