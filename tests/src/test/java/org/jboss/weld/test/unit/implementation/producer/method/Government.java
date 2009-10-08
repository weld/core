package org.jboss.weld.test.unit.implementation.producer.method;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SessionScoped
public class Government implements Serializable
{

   private static final long serialVersionUID = 1L;

   @Important Car governmentCar;
   
   public void destabilize()
   {
      
   }
   
   
}
