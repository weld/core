package org.jboss.webbeans.bootstrap;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.AfterBeanDiscovery;

public class AfterBeanDiscoveryImpl implements AfterBeanDiscovery
{
   private List<Throwable> definitionErrors = new ArrayList<Throwable>();
   
   public void addDefinitionError(Throwable t)
   {
      definitionErrors.add(t);
   }
   
   public List<Throwable> getDefinitionErrors()
   {
      return definitionErrors;
   }

}
