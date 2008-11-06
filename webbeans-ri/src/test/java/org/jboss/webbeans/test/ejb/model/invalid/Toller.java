package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Observes;

import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.beans.Horse;

@Stateful
public class Toller
{
   @Remove
   public void destroy(@Observes @Tame Horse horse) {
      
   }
}
