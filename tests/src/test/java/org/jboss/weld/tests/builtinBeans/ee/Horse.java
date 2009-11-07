package org.jboss.weld.tests.builtinBeans.ee;

import javax.ejb.Stateful;


@Stateful
public class Horse implements HorseRemote
{
   public boolean ping()
   {
      return true;
   }
}
