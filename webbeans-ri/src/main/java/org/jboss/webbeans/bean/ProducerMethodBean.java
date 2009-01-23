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

package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Disposes;
import javax.webbeans.Observes;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.MetaDataCache;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.jlr.AnnotatedMethodImpl;
import org.jboss.webbeans.util.Names;

/**
 * Represents a producer method bean
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class ProducerMethodBean<T> extends AbstractProducerBean<T, Method>
{
   // The underlying method
   private AnnotatedMethod<T> method;

   private AnnotatedMethod<?> disposalMethod;
   
   private ProducerMethodBean<?> specializedBean;

   /**
    * Creates a producer method Web Bean
    * 
    * @param method The underlying method abstraction
    * @param declaringBean The declaring bean abstraction
    * @param manager the current manager
    * @return A producer Web Bean
    */
   public static <T> ProducerMethodBean<T> of(AnnotatedMethod<T> method, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      return new ProducerMethodBean<T>(method, declaringBean, manager);
   }

   public static <T> ProducerMethodBean<T> of(Method method, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      return of(AnnotatedMethodImpl.<T>of(method, declaringBean.getAnnotatedItem()), declaringBean, manager);
   }
   
   protected ProducerMethodBean(AnnotatedMethod<T> method, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      super(declaringBean, manager);
      this.method = method;
      init();
   }

   public T produceInstance()
   {
      return method.invoke(getReceiver(), manager);
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   protected void init()
   {
      super.init();
      checkProducerMethod();
      initDisposalMethod();
      initInjectionPoints();
   }

   /**
    * Initializes the injection points
    */
   protected void initInjectionPoints()
   {
      for (AnnotatedParameter<?> parameter : method.getParameters())
      {
         annotatedInjectionPoints.add(parameter);
      }
      if (disposalMethod != null)
      {
         for (AnnotatedParameter<?> injectable : disposalMethod.getParameters())
         {
            annotatedInjectionPoints.add(injectable);
         }
      }
   }

   /**
    * Validates the producer method
    */
   protected void checkProducerMethod()
   {
      if (getAnnotatedItem().getAnnotatedParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Observes");
      }
      else if (getAnnotatedItem().getAnnotatedParameters(Disposes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Disposes");
      }
   }

   /**
    * Initializes the remove method
    */
   protected void initDisposalMethod()
   {
      Set<AnnotatedMethod<?>> disposalMethods = manager.resolveDisposalMethods(getType(), getBindings().toArray(new Annotation[0]));
      if (disposalMethods.size() == 1)
      {
         this.disposalMethod = disposalMethods.iterator().next();
      }
      else if (disposalMethods.size() > 1)
      {
         // TODO List out found disposal methods
         throw new DefinitionException("Cannot declare multiple disposal methods for this producer method");
      }
   }

   /**
    * Gets the annotated item representing the method
    * 
    * @return The annotated item
    */
   @Override
   protected AnnotatedMethod<T> getAnnotatedItem()
   {
      return method;
   }

   /**
    * Returns the default name
    * 
    * @return The default name
    */
   @Override
   protected String getDefaultName()
   {
      return method.getPropertyName();
   }

   /**
    * Returns the disposal method
    * 
    * @return The method representation
    */
   public AnnotatedMethod<?> getDisposalMethod()
   {
      return disposalMethod;
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Annotated " + Names.scopeTypeToString(getScopeType()));
      if (getName() == null)
      {
         buffer.append("unnamed producer method bean");
      }
      else
      {
         buffer.append("simple producer method bean '" + getName() + "'");
      }
      buffer.append(" [" + getType().getName() + "]\n");
      buffer.append("   API types " + getTypes() + ", binding types " + getBindings() + "\n");
      return buffer.toString();
   }

   @Override
   public boolean isSerializable()
   {
      boolean passivatingScoped = MetaDataCache.instance().getScopeModel(scopeType).isPassivating();
      if (passivatingScoped)
      {
         return injectionPointsAreSerializable();
      }
      return true;
   }
   
   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      return specializedBean;
   }
   
   @Override
   public boolean isSpecializing()
   {
      // TODO Auto-generated method stub
      return false;
   }

}
