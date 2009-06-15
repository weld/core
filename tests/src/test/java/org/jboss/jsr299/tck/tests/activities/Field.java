package org.jboss.jsr299.tck.tests.activities;

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
