package org.jboss.weld.tests.injectionPoint;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class IntConsumer implements Serializable
{

   private static final long serialVersionUID = 6619645042310126425L;

   @Inject
   private int maxNumber;

   public IntConsumer()
   {
   }
   
   public void ping()
   {
      
   }

}
