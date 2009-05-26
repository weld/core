package org.jboss.webbeans.test.unit.lookup.circular;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Initializer;

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
