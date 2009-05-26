package org.jboss.webbeans.test.unit.lookup.circular;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Initializer;

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
