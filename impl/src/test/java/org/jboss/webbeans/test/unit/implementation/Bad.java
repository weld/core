package org.jboss.webbeans.test.unit.implementation;

import javax.context.RequestScoped;
import javax.ejb.Remove;
import javax.ejb.Stateful;


@Stateful
@RequestScoped
public class Bad implements BadLocal
{
   @Remove
   public void bye()
   {
   }
}
