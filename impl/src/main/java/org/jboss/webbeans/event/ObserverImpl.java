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
import java.util.List;

import javax.context.Dependent;
import javax.event.Asynchronously;
import javax.event.IfExists;
import javax.event.Observer;
import javax.event.ObserverException;
import javax.event.Observes;
import javax.inject.DefinitionException;
import javax.inject.Disposes;
import javax.inject.Initializer;
import javax.inject.Produces;
import javax.inject.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.DependentInstancesStore;
import org.jboss.webbeans.context.DependentStorageRequest;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;

/**
 * <p>
 * Reference implementation for the Observer interface, which represents an
 * observer method. Each observer method has an event type which is the class of
 * the event object being observed, and event binding types that are annotations
 * applied to the event parameter to narrow the event notifications delivered.
 * </p>
 * 
 * @author David Allen
 * 
 */
public class ObserverImpl<T> implements Observer<T>
{
   protected final Bean<?> observerBean;
   protected final MethodInjectionPoint<?> observerMethod;
   private final boolean conditional;
   private final boolean asynchronous;
   protected ManagerImpl manager;
   private final Type eventType;
   private final Annotation[] bindings;

   /**
    * Creates an Observer which describes and encapsulates an observer method
    * (8.5).
    * 
    * @param observer The observer
    * @param observerBean The observer bean
    * @param manager The Web Beans manager
    */
   protected ObserverImpl(final AnnotatedMethod<?> observer, final Bean<?> observerBean, final ManagerImpl manager)
   {
      this.manager = manager;
      this.observerBean = observerBean;
      this.observerMethod = MethodInjectionPoint.of(observerBean, observer);
      this.eventType = observerMethod.getAnnotatedParameters(Observes.class).get(0).getType();

      this.bindings = observerMethod.getAnnotatedParameters(Observes.class).get(0).getBindingsAsArray();
      this.conditional = !observerMethod.getAnnotatedParameters(IfExists.class).isEmpty();
      this.asynchronous = !observerMethod.getAnnotatedParameters(Asynchronously.class).isEmpty();
   }
   
   /**
    * Completes initialization of the observer and allows derived types to
    * override behavior.
    */
   public void initialize()
   {
      checkObserverMethod();
   }



   /**
    * Performs validation of the observer method for compliance with the
    * specifications.
    */
   private void checkObserverMethod()
   {
      // Make sure exactly one and only one parameter is annotated with Observes
      List<AnnotatedParameter<?>> eventObjects = this.observerMethod.getAnnotatedParameters(Observes.class);
      if (eventObjects.size() > 1)
      {
         throw new DefinitionException(this + " is invalid because it contains more than event parameter annotated @Observes");
      }
      // Make sure the event object above is not parameterized with a type
      // variable or wildcard
      if (eventObjects.size() > 0)
      {
         AnnotatedParameter<?> eventParam = eventObjects.iterator().next();
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
      List<AnnotatedParameter<?>> disposeParams = this.observerMethod.getAnnotatedParameters(Disposes.class);
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

   public void notify(final T event)
   {
      if (this.asynchronous)
      {
         sendEventAsynchronously(event);
      }
      else
      {
         sendEvent(event);
      }
   }

   /**
    * Invokes the observer method immediately passing the event.
    * 
    * @param event The event to notify observer with
    */
   protected void sendEvent(final T event)
   {
      Object instance = null;
      DependentStorageRequest dependentStorageRequest = DependentStorageRequest.of(new DependentInstancesStore(), new Object());
      try
      {
         if (Dependent.class.equals(observerBean.getScopeType()) && observerBean instanceof RIBean)
         {
            DependentContext.INSTANCE.startCollectingDependents(dependentStorageRequest);
         }
         // Get the most specialized instance of the component
         instance = getInstance(observerBean);
         if (instance == null)
         {
            return;
         }
         // As we are working with the contextual instance, we may not have the actual object, but a container proxy (e.g. EJB)
         observerMethod.invokeOnInstanceWithSpecialValue(instance, Observes.class, event, manager, null, ObserverException.class);
      }
      finally
      {
         if (Dependent.class.equals(observerBean.getScopeType()))
         {
            DependentContext.INSTANCE.stopCollectingDependents(dependentStorageRequest);
            dependentStorageRequest.getDependentInstancesStore().destroyDependentInstances(dependentStorageRequest.getKey());
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
   
   private <B> B getInstance(Bean<B> observerBean)
   {
      return manager.getInstance(observerBean, !isConditional());
   }

   /**
    * Indicates if the observer is conditional
    * 
    * @return True if conditional, false otherwise
    */
   public boolean isConditional()
   {
      return conditional;
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("Observer Implementation: \n");
      builder.append("  Observer (Declaring) bean: " + observerBean);
      builder.append("  Observer method: " + observerMethod);
      return builder.toString();
   }

   public Type getEventType()
   {
      return eventType;
   }

   public Annotation[] getBindingsAsArray()
   {
      return bindings;
   }

}
