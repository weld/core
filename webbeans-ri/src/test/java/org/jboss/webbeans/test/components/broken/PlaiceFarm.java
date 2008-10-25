package org.jboss.webbeans.test.components.broken;

import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Whitefish;
import org.jboss.webbeans.test.components.Plaice;

@Production
public class PlaiceFarm
{

   @SuppressWarnings("unused")
   @Whitefish
   private Plaice plaice;
   
}
