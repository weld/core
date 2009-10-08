package org.jboss.weld.test.unit.implementation.enterprise;

import javax.ejb.Stateless;

@Stateless
public class FedoraImpl implements Fedora
{

   public void causeRuntimeException()
   {
      throw new BowlerHatException();
   }

}
