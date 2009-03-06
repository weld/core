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

package org.jboss.webbeans.introspector;

import java.lang.reflect.Field;

/**
 * AnnotatedField provides a uniform access to the annotations on an annotated
 * field 
 * 
 * @author Pete Muir
 *
 */
public interface AnnotatedField<T> extends AnnotatedMember<T, Field>
{
   
   /**
    * Gets the annotated field
    * 
    * @return The annotated field
    */
   public Field getAnnotatedField();
   
   /**
    * Injects an instance
    * 
    * 
    * @param declaringInstance The instance to inject into
    * @param value The value to inject
    */
   public void set(Object declaringInstance, Object value) throws IllegalArgumentException, IllegalAccessException;
   
   /**
    * Injects into the field using the value provided by the manager
    * 
    * Unlike {@link #set(Object, Manager)} the field injected into is 
    * discovered from the declaring instance. This is slower, but safe to use
    * with proxies.
    * 
    * @param declaringInstance The instance to inject into
    * @param value The value to inject
    */
   public void setOnInstance(Object declaringInstance, Object value) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException;
   
   public T get(Object instance);
   
   /**
    * Gets an abstraction of the declaring class
    * 
    * @return The declaring class
    */
   public AnnotatedType<?> getDeclaringClass();
   
   /**
    * Gets the property name of the field
    * 
    * @return The name
    */
   public String getPropertyName();

   public boolean isTransient();

}
