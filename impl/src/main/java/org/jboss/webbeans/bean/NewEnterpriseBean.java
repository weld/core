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
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.literal.NewLiteral;

/**
 * Represents a @New enterprise bean
 * 
 * @author Nicklas Karlsson
 */
public class NewEnterpriseBean<T> extends EnterpriseBean<T> implements NewBean
{

   /**
    * Creates an instance of a NewEnterpriseBean from an annotated class
    * 
    * @param clazz The annotated class
    * @param manager The Web Beans manager
    * @return a new NewEnterpriseBean instance
    */
   public static <T> NewEnterpriseBean<T> of(WBClass<T> clazz, BeanManagerImpl manager, BeanDeployerEnvironment environment)
   {
      return new NewEnterpriseBean<T>(clazz, manager, environment);
   }
   
   private Set<Annotation> bindings;

   /**
    * Protected constructor
    * 
    * @param type An annotated class
    * @param manager The Web Beans manager
    */
   protected NewEnterpriseBean(final WBClass<T> type, BeanManagerImpl manager, BeanDeployerEnvironment environment)
   {
      super(type, manager, environment);
      this.bindings = new HashSet<Annotation>();
      this.bindings.add(new NewLiteral()
      {
         
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
   public Class<? extends Annotation> getScopeType()
   {
      return Dependent.class;
   }

   @Override
   public boolean isPolicy()
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
   public Set<Annotation> getBindings()
   {
      return bindings;
   }
   
   @Override
   public boolean isSpecializing()
   {
      return false;
   }
   
   @Override
   protected void checkScopeAllowed()
   {
      // No-op
   }

}
