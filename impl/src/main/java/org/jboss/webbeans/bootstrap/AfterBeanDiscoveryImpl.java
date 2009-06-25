package org.jboss.webbeans.bootstrap;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;

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

   public void addBean(Bean<?> bean)
   {
      throw new UnsupportedOperationException();
   }

   public void addContext(Context context)
   {
      throw new UnsupportedOperationException();
   }

   public void addObserverMethod(ObserverMethod<?, ?> observerMethod)
   {
      throw new UnsupportedOperationException();
   }

}
