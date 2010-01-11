package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

public abstract class AbstractContainerEvent
{

   private final List<Throwable> errors;
   private final BeanManagerImpl beanManager;
   private final Type[] actualTypeArguments;
   private final Type rawType;

   protected AbstractContainerEvent(BeanManagerImpl beanManager, Type rawType, Type[] actualTypeArguments)
   {
      this.errors = new ArrayList<Throwable>();
      this.beanManager = beanManager;
      this.actualTypeArguments = actualTypeArguments;
      this.rawType = rawType;
   }

   /**
    * @return the errors
    */
   protected List<Throwable> getErrors()
   {
      return errors;
   }
   
   protected BeanManagerImpl getBeanManager()
   {
      return beanManager;
   }
   
   protected void fire()
   {
      Type eventType = new ParameterizedTypeImpl(getRawType(), getEmptyTypeArray(), null);
      try
      {
         beanManager.fireEvent(eventType, this);
      }
      catch (Exception e) 
      {
         getErrors().add(e);
      }
   }
   
   protected void fire(Map<BeanDeploymentArchive, BeanDeployment> beanDeployments)
   {
      // Collect all observers to remove dupes
      Set<ObserverMethod<Object>> observers = new HashSet<ObserverMethod<Object>>();
      Type eventType = new ParameterizedTypeImpl(getRawType(), getEmptyTypeArray(), null);
      for (BeanDeployment beanDeployment : beanDeployments.values())
      {
         observers.addAll(beanDeployment.getBeanManager().resolveObserverMethods(eventType));
      }
      for (ObserverMethod<Object> observerMethod : observers)
      {
         observerMethod.notify(this);
      }
   }

   protected Type getRawType()
   {
      return rawType;
   }

   protected Type[] getEmptyTypeArray()
   {
      return actualTypeArguments;
   }

}