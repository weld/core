package org.jboss.webbeans.bootstrap;

import javax.enterprise.inject.spi.AfterBeanDiscovery;

import org.jboss.webbeans.DefinitionException;

public class AfterBeanDiscoveryImpl implements AfterBeanDiscovery
{
   public void addDefinitionError(Throwable t)
   {
      //XXX spec says need to delay abort until all observers
      //have been notified
      throw new DefinitionException(t);
   }

}
