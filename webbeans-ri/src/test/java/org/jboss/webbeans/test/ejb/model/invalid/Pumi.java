package org.jboss.webbeans.test.ejb.model.invalid;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Disposes;

import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.beans.Horse;

@Stateful
public class Pumi
{
   @Remove
   public void destroy(@Disposes @Tame Horse horse) {
      
   }
}
