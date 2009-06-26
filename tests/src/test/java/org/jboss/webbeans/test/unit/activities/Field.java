package org.jboss.webbeans.test.unit.activities;

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
