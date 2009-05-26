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

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;



/**
 * 
 * Provides access to metadata about the injection point into which a dependent
 * object is injected.
 * 
 * @author Gavin King
 * @author Pete Muir
 */
public interface InjectionPoint
{
   
   /**
    * Get the declared type of injection point
    * 
    * If the injection point is declared in XML, the type and binding types are
    * determined according to Section 10.8, �Specifying API types and binding
    * types�.
    * 
    * @return the declared type of the injection point
    */
   public Type getType();
   
   /**
    * Get the declared binding types of the injection point
    * 
    * If the injection point is declared in XML, the type and binding types are
    * determined according to Section 10.8, �Specifying API types and binding
    * types�.
    * 
    * @return the declared binding types of the injection point
    */
   public Set<Annotation> getBindings();
   
   /**
    * Get the Bean object representing the Web Bean that defines the injection
    * point
    * 
    * @return the Bean object representing the Web Bean that defines the
    *         injection point
    */
   public Bean<?> getBean();
   
   /**
    * Get the Field object in the case of field injection, the Method object in
    * the case of method parameter injection or the Constructor object in the
    * case of constructor parameter injection.
    * 
    * @return the member being injected into
    */
   public Member getMember();
   
   /**
    * Get the annotation instance for the given annotation type of the field in
    * the case of field injection, or annotations of the parameter in the case
    * of method parameter or constructor parameter injection.
    * 
    * @param <T>
    *           the type of the annotation
    * @param annotationType
    *           the type of the annotation
    * @return the annotation of the specified annotationType, or null if no such
    *         annotation exists
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationType);
   
   /**
    * Get the annotations of the field in the case of field injection, or
    * annotations of the parameter in the case of method parameter or
    * constructor parameter injection.
    * 
    * @return the annotations of the field
    */
   public Annotation[] getAnnotations();
   
   /**
    * Determine if the specified annotation is present on the injection point
    * 
    * @param annotationType
    *           the type of the annotation
    * @return true if an annotation of the specified type is present, else false
    */
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType);
}
