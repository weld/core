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
package org.jboss.weld.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.introspector.WeldMethod;

public class DisposalMethod<X, T> extends AbstractReceiverBean<X, T, Method>
{

   protected MethodInjectionPoint<T, ?> disposalMethodInjectionPoint;

   protected DisposalMethod(BeanManagerImpl manager, WeldMethod<T, X> disposalMethod, AbstractClassBean<X> declaringBean)
   {
      super(new StringBuilder().append(DisposalMethod.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(declaringBean.getAnnotatedItem().getName()).append(disposalMethod.getSignature().toString()).toString(), declaringBean, manager);
      this.disposalMethodInjectionPoint = MethodInjectionPoint.of(this, disposalMethod);
      initBindings();
      initType();
      initTypes();
      initStereotypes();
      initPolicy();
   }
   
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      addInjectionPoint(disposalMethodInjectionPoint);
      super.initialize(environment);
      checkDisposalMethod();
   }

   @SuppressWarnings("unchecked")
   protected void initType()
   {
      this.type = (Class<T>) disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).get(0).getJavaClass();
   }

   @Override
   public WeldMethod<T, ?> getAnnotatedItem()
   {
      return disposalMethodInjectionPoint;
   }

   public static <X, T> DisposalMethod<X, T> of(BeanManagerImpl manager, WeldMethod<T, X> disposalMethod, AbstractClassBean<X> declaringBean)
   {
      return new DisposalMethod<X, T>(manager, disposalMethod, declaringBean);
   }

   @Override
   protected void initBindings()
   {
      // At least 1 parameter exists, already checked in constructor
      this.bindings = new HashSet<Annotation>();
      this.bindings.addAll(disposalMethodInjectionPoint.getWBParameters().get(0).getQualifiers());
      initDefaultBindings();
   }

   /**
    * Initializes the API types
    */
   @Override
   protected void initTypes()
   {
      Set<Type> types = new HashSet<Type>();
      types.addAll(disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).get(0).getTypeClosure());
      types.add(Object.class);
      super.types = types;
   }

   @Override
   public String getName()
   {
      return null;
   }

   @Override
   public Class<? extends Annotation> getScope()
   {
      return null;
   }

   @Override
   public Set<Type> getTypes()
   {
      return types;
   }

   @Override
   public String getDescription()
   {
      return disposalMethodInjectionPoint.toString();
   }

   @Override
   public boolean isNullable()
   {
      // Not relevant
      return false;
   }

   @Override
   public boolean isSerializable()
   {
      // Not relevant
      return false;
   }

   @Override
   public boolean isProxyable()
   {
      return true;
   }

   public T create(CreationalContext<T> creationalContext)
   {
      // Not Relevant
      return null;
   }

   public void invokeDisposeMethod(Object instance)
   {
      CreationalContext<T> creationalContext = manager.createCreationalContext(this);
      Object receiverInstance = getReceiver(creationalContext);
      if (receiverInstance == null)
      {
         disposalMethodInjectionPoint.invokeWithSpecialValue(null, Disposes.class, instance, manager, creationalContext, IllegalArgumentException.class);
      }
      else
      {
         disposalMethodInjectionPoint.invokeOnInstanceWithSpecialValue(receiverInstance, Disposes.class, instance, manager, creationalContext, IllegalArgumentException.class);
      }
      creationalContext.release();
   }

   private void checkDisposalMethod()
   {
      if (!disposalMethodInjectionPoint.getWBParameters().get(0).isAnnotationPresent(Disposes.class))
      {
         throw new DefinitionException(disposalMethodInjectionPoint.toString() + " doesn't have @Dispose as first parameter");
      }
      if (disposalMethodInjectionPoint.getAnnotatedParameters(Disposes.class).size() > 1)
      {
         throw new DefinitionException(disposalMethodInjectionPoint.toString() + " has more than one @Dispose parameters");
      }
      if (disposalMethodInjectionPoint.getAnnotatedParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException("@Observes is not allowed on disposal method, see " + disposalMethodInjectionPoint.toString());
      }
      if (disposalMethodInjectionPoint.getAnnotation(Inject.class) != null)
      {
         throw new DefinitionException("@Intitializer is not allowed on a disposal method, see " + disposalMethodInjectionPoint.toString());
      }
      if (disposalMethodInjectionPoint.getAnnotation(Produces.class) != null)
      {
         throw new DefinitionException("@Produces is not allowed on a disposal method, see " + disposalMethodInjectionPoint.toString());
      }
      if (getDeclaringBean() instanceof SessionBean<?>)
      {
         boolean methodDeclaredOnTypes = false;
         // TODO use annotated item?
         for (Type type : getDeclaringBean().getTypes())
         {
            if (type instanceof Class<?>)
            {
               Class<?> clazz = (Class<?>) type;
               try
               {
                  clazz.getDeclaredMethod(disposalMethodInjectionPoint.getName(), disposalMethodInjectionPoint.getParameterTypesAsArray());
                  methodDeclaredOnTypes = true;
               }
               catch (NoSuchMethodException nsme)
               {
                  // No - op
               }
            }
         }
         if (!methodDeclaredOnTypes)
         {
            throw new DefinitionException("Producer method " + toString() + " must be declared on a business interface of " + getDeclaringBean());
         }
      }
   }

   @Override
   public Class<T> getType()
   {
      return type;
   }

   @Override
   protected String getDefaultName()
   {
      return disposalMethodInjectionPoint.getPropertyName();
   }

   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      // No-op. Producer method dependent objects are destroyed in producer method bean  
   }

   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      // Doesn't support specialization
      return null;
   }
   
   @Override
   protected void initScopeType()
   {
      // Disposal methods aren't scoped
   }

   @Override
   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return Collections.emptySet();
   }

}
