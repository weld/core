package org.jboss.weld.tests.injectionPoint;

import java.io.Serializable;
import java.util.Timer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class IntGenerator implements Serializable
{

   private static final long serialVersionUID = -7213673465118041882L;

   @Inject
   private Instance<Timer> timerInstance;

   @Produces
   int getInt()
   {
      // This has no purpose other than to invoke a method on the proxy so that the proxy instance
      // is retrieved via the producer method
      timerInstance.get().cancel();
      return 100;
   }

}
