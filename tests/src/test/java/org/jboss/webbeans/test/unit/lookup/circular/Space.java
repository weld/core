package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class Space
{
   
   // For serialization
   public Space() {}
   
   @Initializer
   public Space(Planet planet)
   {
   }
   
}
