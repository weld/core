package org.jboss.weld.tests.activities.current;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

class Field
{
   
   @Inject @Tame Instance<Cow> instance;
   
   public Cow get()
   {
      return instance.get();
   }
   
}
