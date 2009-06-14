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

package javax.enterprise.inject.spi;

import java.util.Set;

/**
 * The metadata for an annotated type which can be parsed by the {@link BeanManager}
 * 
 * The semantics are similar to {@link Class}.
 * 
 * @author Pete Muir
 *
 * @param <X> the type of the class
 */
public interface AnnotatedType<X> extends Annotated {

   /**
    * Get the underlying class instance
    * 
    * @return
    */
   public Class<X> getJavaClass();

   /**
    * Get the constructors belonging to the class
    * 
    * If an empty set is returned, a default (no-args) constructor will be 
    * assumed.
    * 
    * @return the constructors, or an empty set if none are defined
    */
   public Set<AnnotatedConstructor<X>> getConstructors();

   /**
    * Get the business methods belonging to the class.
    * 
    * @return the methods, or an empty set if none are defined
    */
   public Set<AnnotatedMethod<? super X>> getMethods();

   /**
    * Get the fields belonging to the class
    * 
    * @return the fields, or an empty set if none are defined
    */
   public Set<AnnotatedField<? super X>> getFields();
}
