package org.jboss.weld.tests.injectionPoint;

import java.io.Serializable;
import java.util.Timer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class DoubleGenerator implements Serializable
{

   private static final long serialVersionUID = -7213673465118041882L;
   
   @Inject Timer timer;
   
   @Produces
   double getDouble()
   {
      timer.cancel();
      return 11.1;
   }

}
