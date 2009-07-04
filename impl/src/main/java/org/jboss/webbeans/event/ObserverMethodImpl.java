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
package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Notify;
import javax.enterprise.event.ObserverException;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Initializer;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.introspector.WBParameter;
import org.jboss.webbeans.util.Names;

/**
 * <p>
 * Reference implementation for the ObserverMethod interface, which represents an
 * observer method. Each observer method has an event type which is the class of
 * the event object being observed, and event binding types that are annotations
 * applied to the event parameter to narrow the event notifications delivered.
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
   private final Notify notifyType;
   protected final RIBean<?> observerBean;
   protected final MethodInjectionPoint<?> observerMethod;
   protected TransactionPhase transactionPhase;

   /**
    * Creates an Observer which describes and encapsulates an observer method
    * (8.5).
    * 
    * @param observer The observer
    * @param observerBean The observer bean
    * @param manager The Web Beans manager
    */
   protected ObserverMethodImpl(final WBMethod<?> observer, final RIBean<?> observerBean, final BeanManagerImpl manager)
   {
      this.manager = manager;
      this.observerBean = observerBean;
      this.observerMethod = MethodInjectionPoint.of(observerBean, observer);
      this.eventType = observerMethod.getAnnotatedParameters(Observes.class).get(0).getBaseType();

      this.bindings = new HashSet<Annotation>(Arrays.asList(observerMethod.getAnnotatedParameters(Observes.class).get(0).getBindingsAsArray()));
      Observes observesAnnotation = observerMethod.getAnnotatedParameters(Observes.class).get(0).getAnnotation(Observes.class);
      this.notifyType = observesAnnotation.notifyObserver();
      transactionPhase = TransactionPhase.IN_PROGRESS;
   }

   /**
    * Performs validation of the observer method for compliance with the
    * specifications.
    */
   private void checkObserverMethod()
   {
      // Make sure exactly one and only one parameter is annotated with Observes
      List<WBParameter<?>> eventObjects = this.observerMethod.getAnnotatedParameters(Observes.class);
      if (eventObjects.size() > 1)
      {
         throw new DefinitionException(this + " is invalid because it contains more than event parameter annotated @Observes");
      }
      // Make sure the event object above is not parameterized with a type
      // variable or wildcard
      if (eventObjects.size() > 0)
      {
         WBParameter<?> eventParam = eventObjects.iterator().next();
         if (eventParam.isParameterizedType())
         {
            for (Type type : eventParam.getActualTypeArguments())
            {
               if (type instanceof TypeVariable)
               {
                  throw new DefinitionException("Cannot use a type variable " + type + " in an parameterized type " + toString());
               }
               else if (type instanceof WildcardType)
               {
                  throw new DefinitionException("Cannot use a wildcard variable " + type + " in an parameterized type " + toString());
               }
            }
         }
      }
      // Check for parameters annotated with @Disposes
      List<WBParameter<?>> disposeParams = this.observerMethod.getAnnotatedParameters(Disposes.class);
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
      if (this.observerMethod.isAnnotationPresent(Initializer.class))
      {
         throw new DefinitionException(this + " cannot be annotated with @Initializer");
      }

   }

   @SuppressWarnings("unchecked")
   public Bean<X> getBean()
   {
      return (Bean<X>) observerBean;
   }

   public Annotation[] getBindingsAsArray()
   {
      return bindings.toArray(new Annotation[0]);
   }

   public Type getEventType()
   {
      return eventType;
   }

   public Notify getNotify()
   {
      return notifyType;
   }

   public Set<Annotation> getObservedBindings()
   {
      return bindings;
   }

   public Type getObservedType()
   {
      return eventType;
   }

   public TransactionPhase getTransactionPhase()
   {
      return TransactionPhase.IN_PROGRESS;
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
         if (notifyType.equals(Notify.ALWAYS))
         {
            creationalContext = manager.createCreationalContext(observerBean);
         }
         instance = manager.getReference(observerBean, creationalContext);
         if (instance == null)
         {
            return;
         }
         // As we are working with the contextual instance, we may not have the actual object, but a container proxy (e.g. EJB)
         observerMethod.invokeOnInstanceWithSpecialValue(instance, Observes.class, event, manager, creationalContext, ObserverException.class);
      }
      finally
      {
         if (creationalContext != null && Dependent.class.equals(observerBean.getScopeType()))
         {
            creationalContext.release();
         }
      }
   }

   /**
    * Queues the event for later execution
    * @param event
    */
   protected void sendEventAsynchronously(final T event)
   {
      DeferredEventNotification<T> deferredEvent = new DeferredEventNotification<T>(event, this);
      manager.getTaskExecutor().execute(deferredEvent);
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("Observer Implementation: \n");
      builder.append("  Observer (Declaring) class: " + Names.typesToString(observerBean.getTypes()));
      builder.append("  Observer method: " + observerMethod);
      return builder.toString();
   }

}
