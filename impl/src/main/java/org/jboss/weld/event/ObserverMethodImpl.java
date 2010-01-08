/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.event;

import static org.jboss.weld.logging.messages.EventMessage.INVALID_DISPOSES_PARAMETER;
import static org.jboss.weld.logging.messages.EventMessage.INVALID_INITIALIZER;
import static org.jboss.weld.logging.messages.EventMessage.INVALID_PRODUCER;
import static org.jboss.weld.logging.messages.EventMessage.INVALID_SCOPED_CONDITIONAL_OBSERVER;
import static org.jboss.weld.logging.messages.EventMessage.MULTIPLE_EVENT_PARAMETERS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.ObserverException;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Inject;
import javax.inject.Qualifier;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.events.AbstractContainerEvent;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

/**
 * <p>
 * Reference implementation for the ObserverMethod interface, which represents
 * an observer method. Each observer method has an event type which is the class
 * of the event object being observed, and event binding types that are
 * annotations applied to the event parameter to narrow the event notifications
 * delivered.
 * </p>
 * 
 * @author David Allen
 * 
 */
public class ObserverMethodImpl<T, X> implements ObserverMethod<T>
{
   
   public static final String ID_PREFIX = ObserverMethodImpl.class.getPackage().getName();
   
   public static final String ID_SEPARATOR = "-";
   
   private final Set<Annotation> bindings;
   private final Type eventType;
   protected BeanManagerImpl manager;
   private final Reception notifyType;
   protected final RIBean<X> declaringBean;
   protected final MethodInjectionPoint<T, X> observerMethod;
   protected TransactionPhase transactionPhase;
   private final String id;

   private final Set<WeldInjectionPoint<?, ?>> newInjectionPoints;

   /**
    * Creates an Observer which describes and encapsulates an observer method
    * (8.5).
    * 
    * @param observer The observer
    * @param declaringBean The observer bean
    * @param manager The Bean manager
    */
   protected ObserverMethodImpl(final WeldMethod<T, X> observer, final RIBean<X> declaringBean, final BeanManagerImpl manager)
   {
      this.manager = manager;
      this.declaringBean = declaringBean;
      this.observerMethod = MethodInjectionPoint.of(declaringBean, observer);
      this.eventType = observerMethod.getAnnotatedParameters(Observes.class).get(0).getBaseType();
      this.id = new StringBuilder().append(ID_PREFIX).append(ID_SEPARATOR)/*.append(manager.getId()).append(ID_SEPARATOR)*/.append(ObserverMethod.class.getSimpleName()).append(ID_SEPARATOR).append(declaringBean.getBeanClass().getName()).append(".").append(observer.getSignature()).toString();
      this.bindings = new HashSet<Annotation>(observerMethod.getAnnotatedParameters(Observes.class).get(0).getMetaAnnotations(Qualifier.class));
      Observes observesAnnotation = observerMethod.getAnnotatedParameters(Observes.class).get(0).getAnnotation(Observes.class);
      this.notifyType = observesAnnotation.notifyObserver();
      transactionPhase = TransactionPhase.IN_PROGRESS;
      this.newInjectionPoints = new HashSet<WeldInjectionPoint<?, ?>>();
      for (WeldInjectionPoint<?, ?> injectionPoint : Beans.getParameterInjectionPoints(null, observerMethod))
      {
         if (injectionPoint.isAnnotationPresent(New.class))
         {
            this.newInjectionPoints.add(injectionPoint);
         }
      }
   }

   public Set<WeldInjectionPoint<?, ?>> getNewInjectionPoints()
   {
      return newInjectionPoints;
   }

   /**
    * Performs validation of the observer method for compliance with the
    * specifications.
    */
   private void checkObserverMethod()
   {
      // Make sure exactly one and only one parameter is annotated with Observes
      List<WeldParameter<?, X>> eventObjects = this.observerMethod.getAnnotatedParameters(Observes.class);
      if (this.notifyType.equals(Reception.IF_EXISTS) && declaringBean.getScope().equals(Dependent.class))
      {
         throw new DefinitionException(INVALID_SCOPED_CONDITIONAL_OBSERVER, this);
      }
      if (eventObjects.size() > 1)
      {
         throw new DefinitionException(MULTIPLE_EVENT_PARAMETERS, this);
      }
      // Check for parameters annotated with @Disposes
      List<WeldParameter<?, X>> disposeParams = this.observerMethod.getAnnotatedParameters(Disposes.class);
      if (disposeParams.size() > 0)
      {
         throw new DefinitionException(INVALID_DISPOSES_PARAMETER, this);
      }
      // Check annotations on the method to make sure this is not a producer
      // method, initializer method, or destructor method.
      if (this.observerMethod.isAnnotationPresent(Produces.class))
      {
         throw new DefinitionException(INVALID_PRODUCER, this);
      }
      if (this.observerMethod.isAnnotationPresent(Inject.class))
      {
         throw new DefinitionException(INVALID_INITIALIZER, this);
      }

   }

   public Class<X> getBeanClass()
   {
      return declaringBean.getType();
   }

   public Annotation[] getBindingsAsArray()
   {
      return bindings.toArray(new Annotation[0]);
   }

   public Reception getReception()
   {
      return notifyType;
   }

   public Set<Annotation> getObservedQualifiers()
   {
      return bindings;
   }

   public Type getObservedType()
   {
      return eventType;
   }

   public TransactionPhase getTransactionPhase()
   {
      return transactionPhase;
   }

   /**
    * @return the observerMethod
    */
   public MethodInjectionPoint<T, X> getMethod()
   {
      return observerMethod;
   }

   /**
    * Completes initialization of the observer and allows derived types to
    * override behavior.
    */
   public void initialize()
   {
      checkObserverMethod();
   }

   public void notify(final T event)
   {
      if (ignore(event))
      {
         return;
      }
      sendEvent(event);
   }

   /**
    * Invokes the observer method immediately passing the event.
    * 
    * @param event The event to notify observer with
    */
   protected void sendEvent(final T event)
   {
      Object instance = null;
      CreationalContext<?> creationalContext = null;
      try
      {
         if (notifyType.equals(Reception.ALWAYS))
         {
            creationalContext = manager.createCreationalContext(declaringBean);
         }
         instance = manager.getReference(declaringBean, creationalContext, false);
         if (instance == null)
         {
            return;
         }
         // As we are working with the contextual instance, we may not have the
         // actual object, but a container proxy (e.g. EJB)
         observerMethod.invokeOnInstanceWithSpecialValue(instance, Observes.class, event, manager, creationalContext, ObserverException.class);
      }
      finally
      {
         if (creationalContext != null && Dependent.class.equals(declaringBean.getScope()))
         {
            creationalContext.release();
         }
      }
   }
   
   protected boolean ignore(T event)
   {
      Class<?> eventType = event.getClass();
      if (AbstractContainerEvent.class.isAssignableFrom(eventType))
      {
         // This is a container lifeycle event, ensure we are firing to an extension
         if (!Extension.class.isAssignableFrom(getBeanClass()))
         {
            return true;
         }
      }
      return false;
   }
   
   @Override
   public String toString()
   {
      return id;
   }
   
   public String getId()
   {
      return id;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof ObserverMethodImpl<?, ?>)
      {
         ObserverMethodImpl<?, ?> that = (ObserverMethodImpl<?, ?>) obj;
         return this.getId().equals(that.getId());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getId().hashCode();
   }

}
