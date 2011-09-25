/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.decorators.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;

/**
 * @author Marius Bogoevici
 */
public class CustomDecorator implements Decorator<Object>
{
   private final Set<InjectionPoint> injectionPoints;
   private BeanManager beanManager;

   public CustomDecorator(BeanManager beanManager)
   {
      this.beanManager = beanManager;
      injectionPoints = Collections.singleton((InjectionPoint)new CustomInjectionPoint());
   }

   public Type getDelegateType()
   {
      return Window.class;
   }

   public Set<Annotation> getDelegateQualifiers()
   {
      return Collections.emptySet();
   }

   public Set<Type> getDecoratedTypes()
   {
      return Collections.singleton((Type) Window.class);
   }

   public Set<Type> getTypes()
   {
      return Collections.<Type>singleton(CustomWindowFrame.class);
   }

   public Set<Annotation> getQualifiers()
   {
      return Collections.emptySet();
   }

   public Class<? extends Annotation> getScope()
   {
      return Dependent.class;
   }

   public String getName()
   {
      return null;
   }

   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return Collections.emptySet();

   }

   public Class<?> getBeanClass()
   {
      return CustomWindowFrame.class;
   }

   public boolean isAlternative()
   {
      return false;
   }

   public boolean isNullable()
   {
      return false;
   }

   public Set<InjectionPoint> getInjectionPoints()
   {
      return injectionPoints;
   }

   public Object create(CreationalContext<Object> creationalContext)
   {
      CustomWindowFrame customFrame = new CustomWindowFrame();
      customFrame.window = (Window) beanManager.getInjectableReference(injectionPoints.iterator().next(), creationalContext);
      return customFrame;
   }

   public void destroy(Object instance, CreationalContext<Object> creationalContext)
   {
      creationalContext.release();
   }

   class CustomInjectionPoint implements InjectionPoint
   {
      private final WeldClass<?> targetClass;
      private final WeldField<CustomWindowFrame,?> windowField;

      public CustomInjectionPoint()
      {
         ClassTransformer transformer = new ClassTransformer("STATIC_INSTANCE", new TypeStore());
         targetClass = WeldClassImpl.of("STATIC_INSTANCE", CustomWindowFrame.class, transformer);
         windowField = targetClass.getDeclaredWeldField("window");
      }

      public Type getType()
      {
         return Window.class;
      }

      public Set<Annotation> getQualifiers()
      {
         return Collections.<Annotation>singleton(DefaultLiteral.INSTANCE);
      }

      public Bean<?> getBean()
      {
         return CustomDecorator.this;
      }

      public Member getMember()
      {
         return ((AnnotatedField<?>)windowField).getJavaMember();
      }

      public Annotated getAnnotated()
      {
         return windowField;
      }

      public boolean isDelegate()
      {
         return true;
      }

      public boolean isTransient()
      {
         return false;
      }
   }
}
