package org.jboss.webbeans.test.unit.bootstrap;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
class Cow implements Animal
{

   public static boolean mooed = false;
   
   public void moo()
   {
      mooed = true;
   }
   
}
