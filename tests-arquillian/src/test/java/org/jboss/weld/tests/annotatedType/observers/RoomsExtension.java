/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.annotatedType.observers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;

public class RoomsExtension implements Extension
{
   private static class ApplicationScopedLiteral extends AnnotationLiteral<ApplicationScoped> implements ApplicationScoped {} 

   private void removeRoomBean(@Observes ProcessAnnotatedType<Room> pat) {
      if(!pat.getAnnotatedType().isAnnotationPresent(RoomId.class)) {
         pat.veto();
      }
   }
   
   private void addRoomAnnotatedTypes(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
      
      AnnotatedType<Room> wrapped = bm.createAnnotatedType(Room.class);
      
      bbd.addAnnotatedType(new RoomAnnotatedTypeWrapper(wrapped, "hall"));
      bbd.addAnnotatedType(new RoomAnnotatedTypeWrapper(wrapped, "pit"));
   }
   
   private static class RoomAnnotatedTypeWrapper implements AnnotatedType<Room> {

      private AnnotatedType<Room> wrapped;
      private String id;
      
      private RoomAnnotatedTypeWrapper(AnnotatedType<Room> wrapped, String id) {
         this.wrapped = wrapped;
         this.id = id;
      }
      
      @Override
      public Type getBaseType()
      {
         return wrapped.getBaseType();
      }

      @Override
      public Set<Type> getTypeClosure()
      {
         return wrapped.getTypeClosure();
      }

      @Override
      public <T extends Annotation> T getAnnotation(Class<T> annotationType)
      {
         if (ApplicationScoped.class.isAssignableFrom(annotationType)) {
            return (T) new ApplicationScopedLiteral();
         }
         if (RoomId.class.isAssignableFrom(annotationType)) {
            return (T) new RoomId.RoomIdLiteral(id);
         }
         return wrapped.getAnnotation(annotationType);
      }

      @Override
      public Set<Annotation> getAnnotations()
      {
         Set<Annotation> ret = new HashSet<Annotation> ();
         
         ret.add(getAnnotation(ApplicationScoped.class));
         ret.add(getAnnotation(RoomId.class));
         
         return ret;
      }

      @Override
      public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
      {
         if (ApplicationScoped.class.isAssignableFrom(annotationType) || RoomId.class.isAssignableFrom(annotationType)) {
            return true; 
         }
         
         return wrapped.isAnnotationPresent(annotationType);
      }

      @Override
      public Class<Room> getJavaClass()
      {
         return wrapped.getJavaClass();
      }

      @Override
      public Set<AnnotatedConstructor<Room>> getConstructors()
      {
         return wrapped.getConstructors();
      }

      @Override
      public Set<AnnotatedMethod<? super Room>> getMethods()
      {
         return wrapped.getMethods();
      }

      @Override
      public Set<AnnotatedField<? super Room>> getFields()
      {
         return wrapped.getFields();
      }
   }
}
