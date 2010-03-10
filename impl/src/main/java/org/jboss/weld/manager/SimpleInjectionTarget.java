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
package org.jboss.weld.manager;

import static org.jboss.weld.logging.messages.BeanManagerMessage.INJECTION_ON_NON_CONTEXTUAL;
import static org.jboss.weld.logging.messages.BeanManagerMessage.MISSING_BEAN_CONSTRUCTOR_FOUND;
import static org.jboss.weld.logging.messages.BeanMessage.INVOCATION_ERROR;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.util.Beans;

/**
 * @author pmuir
 * 
 */
public class SimpleInjectionTarget<T> implements InjectionTarget<T>
{

   protected final BeanManagerImpl beanManager;
   private final WeldClass<T> type;
   private final ConstructorInjectionPoint<T> constructor;
   protected final List<Set<FieldInjectionPoint<?, ?>>> injectableFields;
   protected final List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;
   private final List<WeldMethod<?, ? super T>> postConstructMethods;
   private final List<WeldMethod<?, ? super T>> preDestroyMethods;
   private final Set<InjectionPoint> injectionPoints;
   protected final Set<WeldInjectionPoint<?, ?>> ejbInjectionPoints;
   protected final Set<WeldInjectionPoint<?, ?>> persistenceContextInjectionPoints;
   protected final Set<WeldInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
   protected final Set<WeldInjectionPoint<?, ?>> resourceInjectionPoints;

   public SimpleInjectionTarget(WeldClass<T> type, BeanManagerImpl beanManager)
   {
      this.beanManager = beanManager;
      this.type = type;
      this.injectionPoints = new HashSet<InjectionPoint>();
      ConstructorInjectionPoint<T> constructor = null;
      try
      {
         constructor = Beans.getBeanConstructor(null, type);
         this.injectionPoints.addAll(Beans.getParameterInjectionPoints(null, constructor));
      }
      catch (Exception e)
      {
         // this means the bean of a type that cannot be produce()d, but that is
         // non-fatal
         // unless someone calls produce()
      }
      this.constructor = constructor;
      this.injectableFields = Beans.getFieldInjectionPoints(null, type);
      this.injectionPoints.addAll(Beans.getFieldInjectionPoints(null, this.injectableFields));
      this.initializerMethods = Beans.getInitializerMethods(null, type);
      this.injectionPoints.addAll(Beans.getParameterInjectionPoints(null, initializerMethods));
      this.postConstructMethods = Beans.getPostConstructMethods(type);
      this.preDestroyMethods = Beans.getPreDestroyMethods(type);
      this.ejbInjectionPoints = Beans.getEjbInjectionPoints(null, type, beanManager);
      this.persistenceContextInjectionPoints = Beans.getPersistenceContextInjectionPoints(null, type, beanManager);
      this.persistenceUnitInjectionPoints = Beans.getPersistenceUnitInjectionPoints(null, type, beanManager);
      this.resourceInjectionPoints = Beans.getResourceInjectionPoints(null, type, beanManager);
      for (InjectionPoint ip : this.injectionPoints)
      {
         if (ip.getType().equals(InjectionPoint.class))
         {
            throw new DefinitionException(INJECTION_ON_NON_CONTEXTUAL, type, ip);
         }
      }
   }

   public T produce(CreationalContext<T> ctx)
   {
      if (constructor == null)
      {
         // this means we couldn't find a constructor on instantiation, which
         // means there isn't one that's spec-compliant
         // try again so the correct DefinitionException is thrown
         Beans.getBeanConstructor(null, type);
         // should not be reached
         throw new ForbiddenStateException(MISSING_BEAN_CONSTRUCTOR_FOUND);
      }
      return constructor.newInstance(beanManager, ctx);
   }

   public void inject(final T instance, final CreationalContext<T> ctx)
   {
      new InjectionContextImpl<T>(beanManager, this, instance)
      {

         public void proceed()
         {
            Beans.injectEEFields(instance, beanManager, ejbInjectionPoints, persistenceContextInjectionPoints, persistenceUnitInjectionPoints, resourceInjectionPoints);
            Beans.injectFieldsAndInitializers(instance, ctx, beanManager, injectableFields, initializerMethods);
         }

      }.run();

   }

   public void postConstruct(T instance)
   {
      for (WeldMethod<?, ? super T> method : postConstructMethods)
      {
         if (method != null)
         {
            try
            {
               // note: RI supports injection into @PreDestroy
               method.invoke(instance);
            }
            catch (Exception e)
            {
               throw new WeldException(INVOCATION_ERROR, e, method, instance);
            }
         }
      }
   }

   public void preDestroy(T instance)
   {
      for (WeldMethod<?, ? super T> method : preDestroyMethods)
      {
         if (method != null)
         {
            try
            {
               // note: RI supports injection into @PreDestroy
               method.invoke(instance);
            }
            catch (Exception e)
            {
               throw new WeldException(INVOCATION_ERROR, e, method, instance);
            }
         }
      }
   }

   public void dispose(T instance)
   {
      // No-op
   }

   public Set<InjectionPoint> getInjectionPoints()
   {
      return injectionPoints;
   }

}
