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
package org.jboss.webbeans.injection;

import static org.jboss.webbeans.injection.Exceptions.rethrowException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import javax.decorator.Decorates;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.introspector.ForwardingWBField;
import org.jboss.webbeans.introspector.WBField;

public class FieldInjectionPoint<T> extends ForwardingWBField<T> implements WBInjectionPoint<T, Field>
{

   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

   private final Bean<?> declaringBean;
   private final WBField<T> field;
   private final boolean delegate;

   public static <T> FieldInjectionPoint<T> of(Bean<?> declaringBean, WBField<T> field)
   {
      return new FieldInjectionPoint<T>(declaringBean, field);
   }

   protected FieldInjectionPoint(Bean<?> declaringBean, WBField<T> field)
   {
      this.declaringBean = declaringBean;
      this.field = field;
      this.delegate = isAnnotationPresent(Decorates.class) && declaringBean instanceof Decorator<?>;
   }

   @Override
   protected WBField<T> delegate()
   {
      return field;
   }

   public Bean<?> getBean()
   {
      return declaringBean;
   }

   public Set<Annotation> getBindings()
   {
      return delegate().getAnnotationStore().getBindings();
   }

   public void inject(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext)
   {
      try
      {
         delegate().set(declaringInstance, manager.getInjectableReference(this, creationalContext));
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e);
      }
      catch (IllegalAccessException e)
      {
         rethrowException(e);
      }
   }

   public void inject(Object declaringInstance, Object value)
   {
      try
      {
         delegate().set(declaringInstance, value);
      }
      catch (IllegalArgumentException e)
      {
         rethrowException(e);
      }
      catch (IllegalAccessException e)
      {
         rethrowException(e);
      }
   }

   public Annotated getAnnotated()
   {
      return delegate();
   }

   public boolean isDelegate()
   {
      return delegate;
   }

   public Type getType()
   {
      return getBaseType();
   }

   public Member getMember()
   {
      return getJavaMember();
   }


}
