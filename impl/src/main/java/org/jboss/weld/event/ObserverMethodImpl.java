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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
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
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Inject;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.api.ExecutorServices;
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
public class ObserverMethodImpl<X, T> implements ObserverMethod<X, T>
{

   private final Set<Annotation> bindings;
   private final Type eventType;
   protected BeanManagerImpl manager;
   private final Reception notifyType;
   protected final RIBean<X> declaringBean;
   protected final MethodInjectionPoint<T, X> observerMethod;
   protected TransactionPhase transactionPhase;

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

      this.bindings = new HashSet<Annotation>(Arrays.asList(observerMethod.getAnnotatedParameters(Observes.class).get(0).getBindingsAsArray()));
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
         throw new DefinitionException(this + " is invalid because it is a conditional observer method, and is declared by a @Dependent scoped bean");
      }
      if (eventObjects.size() > 1)
      {
         throw new DefinitionException(this + " is invalid because it contains more than event parameter annotated @Observes");
      }
      // Make sure the event object above is not parameterized with a type
      // variable or wildcard
      if (eventObjects.size() > 0)
      {
         WeldParameter<?, ?> eventParam = eventObjects.iterator().next();
         if (eventParam.isParameterizedType())
         {
            for (Type type : eventParam.getActualTypeArguments())
            {
               if (type instanceof TypeVariable<?>)
               {
                  throw new DefinitionException("Cannot use a type variable " + type + " in an parameterized type " + toString());
               }
               // else if (type instanceof WildcardType)
               // {
               // throw new
               // DefinitionException("Cannot use a wildcard variable " + type +
               // " in an parameterized type " + toString());
               // }
            }
         }
      }
      // Check for parameters annotated with @Disposes
      List<WeldParameter<?, X>> disposeParams = this.observerMethod.getAnnotatedParameters(Disposes.class);
      if (disposeParams.size() > 0)
      {
         throw new DefinitionException(this + " cannot have any parameters annotated with @Disposes");
      }
      // Check annotations on the method to make sure this is not a producer
      // method, initializer method, or destructor method.
      if (this.observerMethod.isAnnotationPresent(Produces.class))
      {
         throw new DefinitionException(this + " cannot be annotated with @Produces");
      }
      if (this.observerMethod.isAnnotationPresent(Inject.class))
      {
         throw new DefinitionException(this + " cannot be annotated with @Initializer");
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
         // Get the most specialized instance of the component
         if (notifyType.equals(Reception.ALWAYS))
         {
            creationalContext = manager.createCreationalContext(declaringBean);
         }
         instance = manager.getReference(declaringBean, creationalContext);
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

   /**
    * Queues the event for later execution
    * 
    * @param event
    */
   protected void sendEventAsynchronously(final T event)
   {
      DeferredEventNotification<T> deferredEvent = new DeferredEventNotification<T>(event, this);
      Container.instance().deploymentServices().get(ExecutorServices.class).getTaskExecutor().execute(deferredEvent);
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("Observer Implementation: \n");
      builder.append("  Observer (Declaring) class: " + declaringBean.getBeanClass());
      builder.append("  Observer method: " + observerMethod);
      return builder.toString();
   }

}
