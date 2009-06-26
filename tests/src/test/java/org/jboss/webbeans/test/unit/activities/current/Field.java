package org.jboss.webbeans.test.unit.activities.current;

import javax.enterprise.inject.Instance;
import javax.inject.Obtains;

class Field
{
   
   @Obtains @Tame Instance<Cow> instance;
   
   public Cow get()
   {
      return instance.get();
   }
   
}
