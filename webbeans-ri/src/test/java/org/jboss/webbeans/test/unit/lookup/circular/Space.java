package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class Space
{
   
   public Space()
   {
      // TODO Auto-generated constructor stub
   }
   
   @Initializer
   public Space(Planet planet)
   {
   }
   
}
