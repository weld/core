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
package org.jboss.weld.introspector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.enterprise.inject.spi.AnnotatedConstructor;

/**
 * Represents a Class Constructor
 * 
 * @author Pete Muir
 * 
 */
public interface WeldConstructor<T> extends WeldCallable<T, T, Constructor<T>>, AnnotatedConstructor<T>
{

   /**
    * Creates a new instance of the class, using this constructor
    * 
    * @return The created instance
    */
   public T newInstance(Object... parameters) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException;

   public ConstructorSignature getSignature();

}
