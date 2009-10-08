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

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Represents a meta annotation
 * 
 * @author Pete Muir
 *
 */
public interface WeldAnnotation<T extends Annotation> extends WeldClass<T>
{ 
   /**
    * Gets all members
    * 
    * @return A set of abstracted members
    */
   public Set<WeldMethod<?, ?>> getMembers();

   /**
    * Gets all the members annotated with annotationType
    * 
    * @param annotationType The annotation type to match
    * @return A set of abstracted members with the annotation type
    */
   public Set<WeldMethod<?, ?>> getAnnotatedMembers(Class<? extends Annotation> annotationType);

   /**
    * Get an annotation member by name
    * 
    * @param memberName
    * @return
    */
   public <A> WeldMethod<A, ?> getMember(String memberName, WeldClass<A> expectedType);

}
