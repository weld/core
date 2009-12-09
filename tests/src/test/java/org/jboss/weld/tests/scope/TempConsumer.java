package org.jboss.weld.tests.scope;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TempConsumer
{

   @Inject @Special Temp specialTemp;
   @Inject @Useless Temp uselessTemp;
   
   public Temp getSpecialTemp()
   {
      return specialTemp;
   }
   
   public Temp getUselessTemp()
   {
      return uselessTemp;
   }
   
   
}
