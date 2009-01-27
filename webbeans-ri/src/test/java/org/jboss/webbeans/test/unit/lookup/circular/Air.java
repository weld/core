package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class Air
{
   @Initializer
   public Air(Fish fish)
   {
   }
   
}
