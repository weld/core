package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class Space
{
   @Initializer
   public Space(Fish fish)
   {
   }
   
}
