package org.jboss.weld.tests.builtinBeans;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class CowEventObserver implements Serializable
{
   
   private boolean observed;
   
   public void observeEvent(@Observes Cow cow)
   {
      this.observed = true;
   }
   
   public boolean isObserved()
   {
      return observed;
   }
   
   public void reset()
   {
      this.observed = false;
   }

}
