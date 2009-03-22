package org.jboss.webbeans.test.unit.bootstrap.ordering;

import javax.context.ApplicationScoped;

@ApplicationScoped
class Cow implements Animal
{

   public static boolean mooed = false;
   
   public void moo()
   {
      mooed = true;
   }
   
}
