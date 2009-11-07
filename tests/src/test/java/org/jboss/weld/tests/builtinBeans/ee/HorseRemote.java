package org.jboss.weld.tests.builtinBeans.ee;

import javax.ejb.Remote;

@Remote
public interface HorseRemote
{

   public boolean ping();
   
}
