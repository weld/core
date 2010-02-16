/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.builtin;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;
import static org.jboss.weld.util.reflection.Reflections.EMPTY_ANNOTATIONS;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.Container;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Names;

/**
 * Helper implementation for Instance for getting instances
 * 
 * @author Gavin King
 * 
 * @param <T>
 */
public class InstanceImpl<T> extends AbstractFacade<T, Instance<T>> implements Instance<T>, Serializable
{

   private static final long serialVersionUID = -376721889693284887L;

   public static <I> Instance<I> of(InjectionPoint injectionPoint, BeanManagerImpl beanManager)
   {
      return new InstanceImpl<I>(getFacadeType(injectionPoint), injectionPoint.getQualifiers().toArray(EMPTY_ANNOTATIONS), injectionPoint, beanManager);
   }
   
   private InstanceImpl(Type type, Annotation[] qualifiers, InjectionPoint injectionPoint, BeanManagerImpl beanManager)
   {
      super(type, qualifiers, injectionPoint, beanManager);
   }
   
   public T get()
   {      
      Bean<?> bean = getBeanManager().getBean(new ResolvableBuilder().setType(getType()).addQualifiers(getQualifiers()).setDeclaringBean(getInjectionPoint().getBean()).create());
      // Push in an empty CC to ensure that we don't get the CC of whatever is injecting the bean containing the Instance injection point
      try
      {
         Container.instance().services().get(CurrentInjectionPoint.class).pushDummy();
         @SuppressWarnings("unchecked")
         T instance = (T) getBeanManager().getReference(bean, getType(), getBeanManager().createCreationalContext(bean));
         return instance;
      }
      finally
      {
         Container.instance().services().get(CurrentInjectionPoint.class).popDummy();
      }
   }

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      return new StringBuilder().append(Names.annotationsToString(getQualifiers())).append(" Instance<").append(getType()).append(">").toString();
   }
   
   private Set<Bean<?>> getBeans()
   {
      return getBeanManager().getBeans(getType(), getQualifiers());
   }
   
   public Iterator<T> iterator()
   {
      Collection<T> instances = new ArrayList<T>();
      for (Bean<?> bean : getBeans())
      {
         Object object = getBeanManager().getReference(bean, getType(), getBeanManager().createCreationalContext(bean));
         
         @SuppressWarnings("unchecked")
         T instance = (T) object;
         
         instances.add(instance);
      }
      return instances.iterator();
   }

   public boolean isAmbiguous()
   {
      return getBeans().size() > 1;
   }

   public boolean isUnsatisfied()
   {
      return getBeans().size() == 0;
   }

   public Instance<T> select(Annotation... qualifiers)
   {
      return selectInstance(this.getType(), qualifiers);
   }

   public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers)
   {
      return selectInstance(subtype, qualifiers);
   }

   public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers)
   {
      return selectInstance(subtype.getType(), qualifiers);
   }
   
   private <U extends T> Instance<U> selectInstance(Type subtype, Annotation[] newQualifiers)
   {
      return new InstanceImpl<U>(
            subtype,
            Beans.mergeInQualifiers(getQualifiers(), newQualifiers), 
            getInjectionPoint(),
            getBeanManager());
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

      public SerializationProxy(InstanceImpl<?> instance)
      {
         super(instance);
      }
      
      private Object readResolve()
      {
         return InstanceImpl.of(getInjectionPoint(), getBeanManager());
      }
      
   }

}
