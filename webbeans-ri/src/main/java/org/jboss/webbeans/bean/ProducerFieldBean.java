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

import java.lang.reflect.Field;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.jlr.AnnotatedFieldImpl;

/**
 * Represents a producer method bean
 * 
 * @author Pete Muir
 *
 * @param <T>
 */
public class ProducerFieldBean<T> extends ProducerBean<T, Field>
{
   
   private AnnotatedField<T> field;
   
   /**
    * Constructor
    * 
    * @param method The producer method
    * @param declaringBean The declaring bean instance
    */
   public ProducerFieldBean(Field field, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      this(new AnnotatedFieldImpl<T>(field, declaringBean.getAnnotatedItem()), declaringBean, manager);
   }
   
   /**
    * Constructor
    * 
    * @param method The producer method abstraction
    * @param declaringBean The declaring bean
    */
   public ProducerFieldBean(AnnotatedField<T> field, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      super(manager, declaringBean);
      this.field = field;
      init();
   }

   /**
    * Creates an instance of the bean
    * 
    * @returns The instance
    */
   @Override
   public T create()
   {
      T instance = field.get(getReceiver());
      checkReturnValue(instance);
      return instance;
   }

   /**
    * Gets the annotated item representing the method
    * 
    * @return The annotated item
    */
   @Override
   protected AnnotatedField<T> getAnnotatedItem()
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
   
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("ProducerFieldBean:\n");
      buffer.append(super.toString() + "\n");
      buffer.append("Declaring bean: " + declaringBean.toString() + "\n");
      buffer.append("Field: " + field.toString() + "\n");
      return buffer.toString();      
   }

   
}
