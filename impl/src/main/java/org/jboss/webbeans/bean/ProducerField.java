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
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.util.Names;

/**
 * Represents a producer field
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class ProducerField<T> extends AbstractProducerBean<T, Field>
{
   // The underlying field
   private WBField<T, ?> field;
   private final String id;
   
   /**
    * Creates a producer field
    * 
    * @param field The underlying method abstraction
    * @param declaringBean The declaring bean abstraction
    * @param manager the current manager
    * @return A producer field
    */
   public static <T> ProducerField<T> of(WBField<T, ?> field, AbstractClassBean<?> declaringBean, BeanManagerImpl manager)
   {
      return new ProducerField<T>(field, declaringBean, manager);
   }

   /**
    * Constructor
    * 
    * @param method The producer field abstraction
    * @param declaringBean The declaring bean
    * @param manager The Web Beans manager
    */
   protected ProducerField(WBField<T, ?> field, AbstractClassBean<?> declaringBean, BeanManagerImpl manager)
   {
      super(new StringBuilder().append(ProducerField.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(declaringBean.getAnnotatedItem().getName()).append(field.getName()).toString(), declaringBean, manager);
      this.field = field;
      initType();
      initTypes();
      initBindings();
      this.id = new StringBuilder().append(BEAN_ID_PREFIX).append(getClass().getSimpleName()).append(BEAN_ID_SEPARATOR).append(declaringBean.getAnnotatedItem().getName()).append(getAnnotatedItem().getName()).toString();
      initStereotypes();
      initPolicy();
   }
   
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      if (!isInitialized())
      {
         super.initialize(environment);
      }
   }

   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      dispose(instance);
   }

   public void dispose(T instance)
   {
      // No clean up required
   }

   public T produce(CreationalContext<T> ctx)
   {
      return field.get(getReceiver(ctx));
   }


   /**
    * Gets the annotated item representing the field
    * 
    * @return The annotated item
    */
   @Override
   protected WBField<T, ?> getAnnotatedItem()
   {
      return field;
   }

   /**
    * Returns the default name
    * 
    * @return The default name
    */
   @Override
   protected String getDefaultName()
   {
      return field.getPropertyName();
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String getDescription()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Annotated " + Names.scopeTypeToString(getScope()));
      if (getName() == null)
      {
         buffer.append("unnamed producer field bean");
      }
      else
      {
         buffer.append("simple producer field bean '" + getName() + "'");
      }
      buffer.append(" [" + getBeanClass().getName() + "] for class type [" + getType().getName() + "] API types " + getTypes() + ", binding types " + getQualifiers());
      return buffer.toString();
   }
   
   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      return null;
   }
   
   @Override
   public boolean isSpecializing()
   {
      return false;
   }
   
   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return Collections.emptySet();
   }

}
