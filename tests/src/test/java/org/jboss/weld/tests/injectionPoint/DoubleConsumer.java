package org.jboss.weld.tests.injectionPoint;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class DoubleConsumer implements Serializable
{

   private static final long serialVersionUID = 6619645042310126425L;

   @Inject
   private double maxNumber;

   public DoubleConsumer()
   {
   }
   
   public void ping()
   {
      
   }

}
