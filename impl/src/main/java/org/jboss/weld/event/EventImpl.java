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

import static org.jboss.weld.logging.messages.EventMessage.PROXY_REQUIRED;
import static org.jboss.weld.util.reflection.Reflections.EMPTY_ANNOTATIONS;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.bean.builtin.AbstractFacade;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Names;
import org.jboss.weld.util.Observers;

/**
 * Implementation of the Event interface
 * 
 * @author David Allen
 * 
 * @param <T> The type of event being wrapped
 * @see javax.enterprise.event.Event
 */
public class EventImpl<T> extends AbstractFacade<T, Event<T>> implements Event<T>, Serializable
{
   
   private static final long serialVersionUID = 656782657242515455L;
   private static final Default DEFAULT = new DefaultLiteral();

   public static <E> EventImpl<E> of(InjectionPoint injectionPoint, BeanManagerImpl beanManager)
   {
      return new EventImpl<E>(getFacadeType(injectionPoint), getFacadeEventQualifiers(injectionPoint), injectionPoint, beanManager);
   }
   
   private static Annotation[] getFacadeEventQualifiers(InjectionPoint injectionPoint)
   {
      if (!injectionPoint.getAnnotated().isAnnotationPresent(Default.class))
      {
         Set<Annotation> qualifers = new HashSet<Annotation>(injectionPoint.getQualifiers());
         qualifers.remove(DEFAULT);
         return qualifers.toArray(EMPTY_ANNOTATIONS);
      }
      else
      {
         return injectionPoint.getQualifiers().toArray(EMPTY_ANNOTATIONS);
      }
   }
   
   private EventImpl(Type type, Annotation[] qualifiers, InjectionPoint injectionPoint, BeanManagerImpl beanManager)
   {
      super(type, qualifiers, injectionPoint, beanManager);
   }

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      return new StringBuilder().append(Names.annotationsToString(getQualifiers())).append(" Event<").append(getType()).append(">").toString();
   }

   public void fire(T event)
   {
      getBeanManager().fireEvent(event, getQualifiers());
   }
   
   public Event<T> select(Annotation... qualifiers)
   {
      return selectEvent(this.getType(), qualifiers);
   }

   public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers)
   {
      return selectEvent(subtype, qualifiers);
   }

   public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers)
   {
      return selectEvent(subtype.getType(), qualifiers);
   }
   
   private <U extends T> Event<U> selectEvent(Type subtype, Annotation[] newQualifiers)
   {
      Observers.checkEventObjectType(subtype);
      return new EventImpl<U>(subtype, Beans.mergeInQualifiers(getQualifiers(), newQualifiers), getInjectionPoint(), getBeanManager());
   }
   
   // Serialization
   
   private Object writeReplace() throws ObjectStreamException
   {
      return new SerializationProxy(this);
   }
   
   private void readObject(ObjectInputStream stream) throws InvalidObjectException
   {
      throw new InvalidObjectException(PROXY_REQUIRED);
   }
   
   private static class SerializationProxy extends AbstractFacadeSerializationProxy
   {

      private static final long serialVersionUID = 9181171328831559650L;

      public SerializationProxy(EventImpl<?> event)
      {
         super(event);
      }
      
      private Object readResolve()
      {
         return EventImpl.of(getInjectionPoint(), getBeanManager());
      }
      
   }

}
