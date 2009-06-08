package org.jboss.webbeans.test.unit.implementation.producer.method;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SessionScoped
public class Government implements Serializable
{

   @Important Car governmentCar;
   
   public void destabilize()
   {
      
   }
   
   
}
