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

import javax.enterprise.inject.spi.AnnotatedField;

/**
 * AnnotatedField provides a uniform access to the annotations on an annotated
 * field 
 * 
 * @author Pete Muir
 *
 */
public interface WBField<T, X> extends WBMember<T, X, Field>, AnnotatedField<X>
{

   /**
    * Injects an instance
    * 
    * 
    * @param declaringInstance The instance to inject into
    * @param value The value to inject
    */
   public void set(Object declaringInstance, Object value) throws IllegalArgumentException, IllegalAccessException;

   public T get(Object instance);

   /**
    * Gets the property name of the field
    * 
    * @return The name
    */
   public String getPropertyName();

   public boolean isTransient();

}
