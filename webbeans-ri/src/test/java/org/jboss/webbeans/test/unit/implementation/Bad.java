package org.jboss.webbeans.test.unit.implementation;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.RequestScoped;


@Stateful
@RequestScoped
public class Bad implements BadLocal
{
   @Remove
   public void bye()
   {
   }
}
