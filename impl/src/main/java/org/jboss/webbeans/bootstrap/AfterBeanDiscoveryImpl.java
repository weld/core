package org.jboss.webbeans.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.webbeans.BeanManagerImpl;

public class AfterBeanDiscoveryImpl implements AfterBeanDiscovery
{
   
   private final List<Throwable> definitionErrors = new ArrayList<Throwable>();
   private final BeanManagerImpl beanManager;
   
   public AfterBeanDiscoveryImpl(BeanManagerImpl beanManager)
   {
      this.beanManager = beanManager;
   }

   public void addDefinitionError(Throwable t)
   {
      definitionErrors.add(t);
   }
   
   public List<Throwable> getDefinitionErrors()
   {
      return Collections.unmodifiableList(definitionErrors);
   }
   
   public void addBean(Bean<?> bean)
   {
      beanManager.addBean(bean);
   }

   public void addContext(Context context)
   {
      beanManager.addContext(context);
   }

   public void addObserverMethod(ObserverMethod<?, ?> observerMethod)
   {
      throw new UnsupportedOperationException();
   }

}
