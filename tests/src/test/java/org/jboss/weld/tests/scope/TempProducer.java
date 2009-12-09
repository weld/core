package org.jboss.weld.tests.scope;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Named
@SessionScoped
public class TempProducer implements Serializable
{

   @Produces
   @RequestScoped
   @Special
   public Temp getTemp()
   {
      return new Temp(10);
   }

   @Produces
   @RequestScoped
   @Useless
   Temp t = new Temp(11);

}