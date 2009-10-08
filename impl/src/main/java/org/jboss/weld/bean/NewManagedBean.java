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
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.introspector.WBClass;
import org.jboss.weld.literal.NewLiteral;

/**
 * Represents a @New simple bean
 * 
 * @author Nicklas Karlsson 
 */
public class NewManagedBean<T> extends ManagedBean<T> implements NewBean
{

   /**
    * Creates an instance of a NewSimpleBean from an annotated class
    * 
    * @param clazz The annotated class
    * @param manager The Bean manager
    * @return a new NewSimpleBean instance
    */
   public static <T> NewManagedBean<T> of(WBClass<T> clazz, BeanManagerImpl manager)
   {
      return new NewManagedBean<T>(clazz, new StringBuilder().append(NewManagedBean.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(clazz.getName()).toString(), manager);
   }
   
   private Set<Annotation> bindings;

   /**
    * Protected constructor
    * 
    * @param type An annotated class
    * @param manager The Bean manager
    */
   protected NewManagedBean(final WBClass<T> type, String idSuffix, BeanManagerImpl manager)
   {
      super(type, idSuffix, manager);
      this.bindings = new HashSet<Annotation>();
      this.bindings.add(new NewLiteral()
      {
         
         @Override
         public Class<?> value()
         {
            return type.getJavaClass();
         }
         
      });
   }

   /**
    * Gets the scope type
    * 
    * @return @Dependent
    */
   @Override
   public Class<? extends Annotation> getScope()
   {
      return Dependent.class;
   }

   @Override
   public boolean isAlternative()
   {
      return false;
   }

   /**
    * Gets the name of the bean
    * 
    * @return null
    */
   @Override
   public String getName()
   {
      return null;
   }

   /**
    * Gets the bindings
    * 
    * @returns @New
    */
   @Override
   public Set<Annotation> getQualifiers()
   {
      return bindings;
   }

   @Override
   public boolean isSpecializing()
   {
      return false;
   }
   
}
