package org.jboss.webbeans.test.unit.lookup.circular;

import javax.context.ApplicationScoped;
import javax.inject.Initializer;

@ApplicationScoped
class Air
{
   
   public Air()
   {
   
   }
   
   @Initializer
   public Air(Bird bird)
   {
   }
   
}
