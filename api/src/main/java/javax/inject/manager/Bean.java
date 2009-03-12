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

package javax.inject.manager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.context.Contextual;

/**
 * The contract between the manager and a bean. This interface
 * should not be called directly by the application.
 * 
 * @author Gavin King
 * 
 * @param <T> an API type of the bean
 */
public abstract class Bean<T> implements Contextual<T>
{
   private final Manager manager;

   /**
    * Create an instance of a bean
    * 
    * @param manager
    */
   protected Bean(Manager manager)
   {
      this.manager = manager;
   }

   /**
    * Get the manager used to create this bean
    * 
    * @return an instance of the manager
    */
   protected Manager getManager()
   {
      return manager;
   }

   /**
    * The client-visible types of a bean
    * 
    * @return the bean types
    */
   public abstract Set<? extends Type> getTypes();

   /**
    * The bindings of a bean
    * 
    * @return the bindings
    */
   public abstract Set<Annotation> getBindings();

   /**
    * The scope of a bean
    * 
    * @return the scope
    */
   public abstract Class<? extends Annotation> getScopeType();

   /**
    * The deployment type of a bean
    * 
    * @return the deployment type
    */
   public abstract Class<? extends Annotation> getDeploymentType();

   /**
    * The name of a bean
    * 
    * @return the name
    */
   public abstract String getName();

   /**
    * The serializability of a bean
    * 
    * @return true if the bean is serializable
    */
   public abstract boolean isSerializable();

   /**
    * The nullability of a bean
    * 
    * @return true if the bean is nullable
    */
   public abstract boolean isNullable();

   /**
    * The injection points of a bean
    * 
    * @return the injection points of a bean
    */
   public abstract Set<? extends InjectionPoint> getInjectionPoints();

}
