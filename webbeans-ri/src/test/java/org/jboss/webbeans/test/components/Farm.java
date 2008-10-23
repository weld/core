package org.jboss.webbeans.test.components;

import javax.webbeans.Current;
import javax.webbeans.Production;

@Production
public class Farm
{

   @Current
   private Pig pig;
   
}
