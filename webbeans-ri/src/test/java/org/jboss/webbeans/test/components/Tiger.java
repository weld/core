package org.jboss.webbeans.test.components;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Synchronous;

@Production
@Stateful
@Synchronous
public class Tiger
{
   
   @Remove
   public void remove()
   {
      
   }

}
